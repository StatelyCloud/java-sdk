package cloud.stately.statelydb.auth;

import cloud.stately.statelydb.common.FutureUtils;
import cloud.stately.statelydb.common.StatelyException;
import java.net.URI;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AuthTokenProvider provides access tokens for authenticating with the Stately Cloud API. This
 * class manages token validation and refresh scheduling, while the StatelyAccessTokenFetcher
 * handles the actual token retrieval from the StatelyDB API as well as retry logic.
 *
 * <p>Example usage with builder pattern:
 *
 * <pre>{@code
 * // Using explicit access key
 * AuthTokenProvider provider = AuthTokenProvider.builder(scheduler)
 *     .accessKey("your-access-key")
 *     .build();
 *
 * // Using environment variable for access key
 * AuthTokenProvider provider = AuthTokenProvider.builder(scheduler)
 *     .build();
 *
 * // With custom endpoint and retry settings
 * AuthTokenProvider provider = AuthTokenProvider.builder(scheduler)
 *     .accessKey("your-access-key")
 *     .baseRetryBackoffSecs(2)
 *     .build();
 * }</pre>
 */
public class AuthTokenProvider implements TokenProvider {

  /** Default base retry backoff in seconds. */
  private static final long DEFAULT_BASE_RETRY_BACKOFF_SECS = 1;

  /** Minimum jitter percentage (90%). */
  private static final double JITTER_PERCENTAGE_MIN = 0.9;

  /** Jitter percentage range (5%), resulting in a total range of 90-95%. */
  private static final double JITTER_PERCENTAGE_RANGE = 0.05;

  /** The token fetcher used to retrieve access tokens. */
  private AtomicReference<TokenFetcher> tokenFetcher = new AtomicReference<>(null);

  /**
   * The current token state, which includes the token and its expiration time. This is updated
   * atomically after a successful token fetch.
   */
  private final AtomicReference<TokenState> currentTokenState = new AtomicReference<>(null);

  /**
   * A CompletableFuture that represents a pending token refresh operation. This is used to ensure
   * that only one refresh operation is in progress at a time.
   */
  private final AtomicReference<CompletableFuture<String>> pendingRefresh =
      new AtomicReference<>(null);

  /**
   * A CompletableFuture that represents a scheduled background refresh operation. This is used to
   * schedule periodic token refreshes based on the fetched token's expiration time.
   */
  private final AtomicReference<CompletableFuture<Void>> scheduledBackgroundRefresh =
      new AtomicReference<>(null);

  /** The scheduler used for managing retries and delays. */
  private final ScheduledExecutorService scheduler;

  /** The access key used for authentication. */
  private final String accessKey;

  /** The base retry backoff in seconds. */
  private final long baseRetryBackoffSecs;

  /**
   * Constructs an AuthTokenProvider with a custom endpoint.
   *
   * @param accessKey The access key for authentication
   * @param baseRetryBackoffSecs The base retry backoff in seconds
   * @param scheduler The scheduler for managing retries
   */
  public AuthTokenProvider(
      String accessKey, long baseRetryBackoffSecs, ScheduledExecutorService scheduler) {
    this.accessKey = accessKey;
    this.baseRetryBackoffSecs = baseRetryBackoffSecs;
    this.scheduler = scheduler;
  }

  /**
   * Creates a new builder for AuthTokenProvider.
   *
   * @param scheduler The scheduler for managing retries (required)
   * @return A new Builder instance
   */
  public static Builder builder(ScheduledExecutorService scheduler) {
    return new Builder(scheduler);
  }

  /** Builder for AuthTokenProvider. Required fields: scheduler. */
  public static class Builder {
    private String accessKey;
    private long baseRetryBackoffSecs = DEFAULT_BASE_RETRY_BACKOFF_SECS;
    private final ScheduledExecutorService scheduler;

    private Builder(ScheduledExecutorService scheduler) {
      this.scheduler = scheduler;
    }

    /**
     * Sets the access key for authentication. If not set, will attempt to read from the
     * STATELY_ACCESS_KEY environment variable.
     *
     * @param accessKey The access key for authentication
     * @return This builder instance
     */
    public Builder accessKey(String accessKey) {
      this.accessKey = accessKey;
      return this;
    }

    /**
     * Sets the base retry backoff time in seconds. If not set, uses the default value.
     *
     * @param baseRetryBackoffSecs The base retry backoff in seconds
     * @return This builder instance
     */
    public Builder baseRetryBackoffSecs(long baseRetryBackoffSecs) {
      this.baseRetryBackoffSecs = baseRetryBackoffSecs;
      return this;
    }

    /**
     * Builds a new AuthTokenProvider instance.
     *
     * @return A new AuthTokenProvider instance
     * @throws StatelyException If STATELY_ACCESS_KEY environment variable is not set when accessKey
     *     is null
     */
    public AuthTokenProvider build() {
      String finalAccessKey = accessKey;
      if (finalAccessKey == null) {
        finalAccessKey = getAccessKeyFromEnvironment();
      }

      return new AuthTokenProvider(finalAccessKey, baseRetryBackoffSecs, scheduler);
    }
  }

  /**
   * Helper method to get the access key from the STATELY_ACCESS_KEY environment variable. This will
   * check both the system property and the environment variable.
   *
   * @return The access key from the environment
   * @throws StatelyException If STATELY_ACCESS_KEY environment variable is not set
   */
  private static String getAccessKeyFromEnvironment() {
    String accessKey = System.getProperty("STATELY_ACCESS_KEY");
    if (accessKey == null) {
      accessKey = System.getenv("STATELY_ACCESS_KEY");
    }

    if (accessKey == null || accessKey.trim().isEmpty()) {
      throw new StatelyException(
          "Unable to find an access key in the STATELY_ACCESS_KEY "
              + "environment variable. Either pass your credentials in "
              + "the options when creating a client or set this environment variable.",
          io.grpc.Status.Code.UNAUTHENTICATED,
          "Unauthenticated");
    }
    return accessKey;
  }

  /**
   * Start the token provider with the given URI.
   *
   * @param uri The URI to start the token provider with
   */
  @Override
  public void start(URI uri) {
    // This can be non null if the customer creates a custom token provider and
    // starts it themselves. If start is called multiple times we just update
    // the endpoint.
    TokenFetcher oldFetcher =
        this.tokenFetcher.getAndSet(
            StatelyAccessTokenFetcher.builder(uri, accessKey, scheduler)
                .baseRetryBackoffSecs(baseRetryBackoffSecs)
                .build());
    if (oldFetcher != null) {
      try {
        oldFetcher.close();
      } catch (Exception e) {
        // Ignore errors closing the old fetcher.
      }
    }

    // kick off a background refresh to prime the cache.
    refreshToken();
  }

  /**
   * Get the current access token.
   *
   * @param force Whether to force a refresh of the token
   * @return A CompletableFuture containing the current access token
   * @throws StatelyException If the token cannot be retrieved
   */
  @Override
  public CompletableFuture<String> getToken(boolean force) {

    if (force) {
      currentTokenState.set(null);
    } else {
      String validToken = validToken();
      if (validToken != null) {
        return CompletableFuture.completedFuture(validToken);
      }
    }

    return refreshToken();
  }

  /**
   * Close the token provider and kill any background operations. This method should be idempotent
   * and safe to call multiple times.
   */
  @Override
  public void close() throws StatelyException {
    try {
      // Cancel any pending refresh operations
      CompletableFuture<String> pending = pendingRefresh.getAndSet(null);
      if (pending != null) {
        pending.cancel(true);
      }

      // Cancel any scheduled background refresh
      CompletableFuture<Void> backgroundRefresh = scheduledBackgroundRefresh.getAndSet(null);
      if (backgroundRefresh != null) {
        backgroundRefresh.cancel(true);
      }

      TokenFetcher tokenFetcher = this.tokenFetcher.getAndSet(null);
      if (tokenFetcher != null) {
        tokenFetcher.close();
      }

    } catch (Exception e) {
      throw StatelyException.from(e);
    }
  }

  private String validToken() {
    TokenState tokenState = currentTokenState.get();
    if (tokenState == null || Instant.now().isAfter(tokenState.getExpiresAt())) {
      return null;
    }
    return tokenState.getToken();
  }

  private CompletableFuture<String> refreshToken() {
    while (true) {
      CompletableFuture<String> pending = pendingRefresh.get();
      if (pending != null) {
        // If there's an ongoing refresh, wait for it
        return pending;
      }

      // Create a new dummy future that we can fill if we manage to set it
      // This won't actually execute until we call refreshTokenImpl()
      CompletableFuture<String> newRefresh = new CompletableFuture<>();
      if (pendingRefresh.compareAndSet(null, newRefresh)) {
        // if we successfully set the pending refresh, run the actual refresh
        refreshTokenImpl()
            .whenComplete(
                (res, err) -> {
                  // Clear the pending refresh when complete (success or failure)
                  // and propagate the result or error to newRefresh.
                  pendingRefresh.set(null);
                  if (err != null) {
                    newRefresh.completeExceptionally(err);
                  } else {
                    newRefresh.complete(res);
                  }
                });

        return newRefresh;
      }
      // If we failed to set, cancel the dummy future and retry
      newRefresh.cancel(true);
    }
  }

  private CompletableFuture<String> refreshTokenImpl() {
    // just assume this is not null because we call start in the client constructor.
    // All of our integ tests will fail if someone ever removes it for some reason.
    TokenFetcher tokenFetcher = this.tokenFetcher.get();
    CompletableFuture<TokenResult> fetchFuture = tokenFetcher.fetch();
    return fetchFuture.thenApply(
        tokenResult -> {
          Instant fetchedExpiry = Instant.now().plusSeconds(tokenResult.getExpiresInSecs());

          // Update the current token state atomically (pure function, no side effects)
          // There should only be one refreshTokenImpl running at a time but a force refresh
          // can clear the token state.
          TokenState newState =
              currentTokenState.updateAndGet(
                  current -> {
                    // Update only if it's null or the new expiry is later
                    if (current == null || fetchedExpiry.isAfter(current.getExpiresAt())) {
                      return new TokenState(tokenResult.getToken(), fetchedExpiry);
                    }
                    return current;
                  });

          // Schedule background refresh outside of the atomic operation.
          // This will cancel any pending refresh if it exists
          scheduleBackgroundRefresh(newState.getExpiresAt());

          return newState.getToken();
        });
  }

  private void scheduleBackgroundRefresh(Instant tokenExpiry) {
    // Calculate refresh time with jitter (90-95% of token lifetime)
    double jitter =
        JITTER_PERCENTAGE_MIN
            + (ThreadLocalRandom.current().nextDouble() * JITTER_PERCENTAGE_RANGE);
    long tokenLifetimeMillis = tokenExpiry.toEpochMilli() - Instant.now().toEpochMilli();
    long delayMillis = (long) (tokenLifetimeMillis * jitter);

    // Create new background refresh future
    CompletableFuture<Void> newBackgroundRefresh =
        FutureUtils.setTimeout(delayMillis, scheduler)
            .thenCompose(v -> refreshToken())
            // Always return null, swallow errors. They will be exposed on the next getToken call.
            .handle((token, ex) -> null);

    // Try to install the new background refresh, canceling any existing one
    CompletableFuture<Void> previousRefresh =
        scheduledBackgroundRefresh.getAndSet(newBackgroundRefresh);
    if (previousRefresh != null && !previousRefresh.isDone()) {
      previousRefresh.cancel(true);
    }
  }
}
