package cloud.stately.statelydb.auth;

import cloud.stately.auth.AuthServiceGrpc;
import cloud.stately.auth.GetAuthTokenRequest;
import cloud.stately.auth.GetAuthTokenResponse;
import cloud.stately.statelydb.common.FutureUtils;
import cloud.stately.statelydb.common.StatelyException;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

/**
 * StatelyAccessTokenFetcher is a TokenFetcher that fetches tokens from the StatelyDB API. This
 * class is responsible for making the actual gRPC calls to the StatelyDB AuthService and handling
 * retries with exponential backoff.
 *
 * <p>Example usage with builder pattern:
 *
 * <pre>{@code
 * StatelyAccessTokenFetcher fetcher = StatelyAccessTokenFetcher.builder(
 *     new URI("https://api.stately.cloud:443"),
 *     "your-access-key",
 *     scheduler)
 *     .baseRetryBackoffSecs(1)
 *     .build();
 * }</pre>
 */
public class StatelyAccessTokenFetcher implements TokenFetcher {

  /** List of Non-retryable errors. */
  private static final Set<Status.Code> NON_RETRYABLE_ERRORS =
      Set.of(
          Status.Code.UNAUTHENTICATED,
          Status.Code.PERMISSION_DENIED,
          Status.Code.NOT_FOUND,
          Status.Code.UNIMPLEMENTED,
          Status.Code.INVALID_ARGUMENT);

  /** Number of retry attempts for requests. */
  private static final int RETRY_ATTEMPTS = 10;

  /** The access key credential for StatelyDB. */
  private final String accessKey;

  /** The base retry backoff time in seconds. */
  private final long baseRetryBackoffSecs;

  /**
   * The scheduled executor service that will be used for scheduling retries and delays. This must
   * be managed by the caller. It is not closed by this class.
   */
  private final ScheduledExecutorService scheduler;

  /** The gRPC channel used to communicate with the StatelyDB API. */
  private final ManagedChannel channel;

  /** The gRPC stub for the AuthService. */
  private final AuthServiceGrpc.AuthServiceFutureStub stub;

  /** Set to track ongoing fetch operations so they can be cancelled when close() is called. */
  private final Set<CompletableFuture<TokenResult>> ongoingFetches = ConcurrentHashMap.newKeySet();

  /** Maximum backoff attempts to prevent overflow. */
  private static final long MAX_BACKOFF_ATTEMPTS = 10;

  /**
   * Creates a new StatelyAccessTokenFetcher.
   *
   * @param endpoint The endpoint of the StatelyDB Auth service. If the protocol is "http",
   *     plaintext (no TLS) will be used automatically for testing purposes.
   * @param accessKey The StatelyDB access key credential
   * @param baseRetryBackoffSecs The base backoff time in seconds for retry attempts
   * @param scheduler The scheduled executor service used for retries and delays
   */
  public StatelyAccessTokenFetcher(
      URI endpoint,
      String accessKey,
      long baseRetryBackoffSecs,
      ScheduledExecutorService scheduler) {
    this.accessKey = accessKey;
    this.baseRetryBackoffSecs = baseRetryBackoffSecs;
    this.scheduler = scheduler;

    ManagedChannelBuilder<?> channelBuilder =
        ManagedChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPort())
            .executor(scheduler)
            .maxInboundMetadataSize(
                Integer.MAX_VALUE); // disabled so that large error details don't cause issues

    if (endpoint.getScheme().equals("http")) {
      channelBuilder.usePlaintext();
    }

    this.channel = channelBuilder.build();
    this.stub = AuthServiceGrpc.newFutureStub(this.channel);
  }

  /**
   * Creates a new builder for StatelyAccessTokenFetcher.
   *
   * @param endpoint The endpoint of the StatelyDB Auth service (required)
   * @param accessKey The StatelyDB access key credential (required)
   * @param scheduler The scheduled executor service for retries and delays (required)
   * @return A new Builder instance
   */
  public static Builder builder(
      URI endpoint, String accessKey, ScheduledExecutorService scheduler) {
    return new Builder(endpoint, accessKey, scheduler);
  }

  /** Builder for StatelyAccessTokenFetcher. Required fields: endpoint, accessKey, scheduler. */
  public static class Builder {
    private final URI endpoint;
    private final String accessKey;
    private long baseRetryBackoffSecs = 1; // Default value
    private final ScheduledExecutorService scheduler;

    private Builder(URI endpoint, String accessKey, ScheduledExecutorService scheduler) {
      this.endpoint = endpoint;
      this.accessKey = accessKey;
      this.scheduler = scheduler;
    }

    /**
     * Sets the base retry backoff time in seconds.
     *
     * @param baseRetryBackoffSecs The base backoff time in seconds
     * @return This builder instance
     */
    public Builder baseRetryBackoffSecs(long baseRetryBackoffSecs) {
      this.baseRetryBackoffSecs = baseRetryBackoffSecs;
      return this;
    }

    /**
     * Builds a new StatelyAccessTokenFetcher instance.
     *
     * @return A new StatelyAccessTokenFetcher instance
     */
    public StatelyAccessTokenFetcher build() {
      return new StatelyAccessTokenFetcher(endpoint, accessKey, baseRetryBackoffSecs, scheduler);
    }
  }

  /**
   * Fetches a new authentication token from the StatelyDB API with automatic retries.
   *
   * @return A CompletableFuture that will complete with the fetched TokenResult
   */
  @Override
  public CompletableFuture<TokenResult> fetch() {
    CompletableFuture<TokenResult> fetchFuture = fetchWithRetries(0);

    // Track this fetch operation so it can be cancelled if close() is called
    ongoingFetches.add(fetchFuture);

    // Remove from tracking when complete (success or failure)
    fetchFuture.whenComplete((result, throwable) -> ongoingFetches.remove(fetchFuture));

    return fetchFuture;
  }

  private CompletableFuture<TokenResult> fetchWithRetries(int attempt) {
    return fetchImpl()
        .handle(
            (result, error) -> {
              if (error == null) {
                return CompletableFuture.completedFuture(result);
              } else {
                return handleError(StatelyException.from(error), attempt);
              }
            })
        .thenCompose(f -> f);
  }

  private CompletableFuture<TokenResult> handleError(StatelyException error, int attempt) {
    // Check if we should retry
    if (attempt >= RETRY_ATTEMPTS || !isRetryableError(error.getGrpcCode())) {
      // No more retries or non-retryable error, propagate the error
      return CompletableFuture.failedFuture(error);
    }

    // Otherwise, wait then retry
    long delayMillis = backoff(attempt, baseRetryBackoffSecs * 1000);
    return FutureUtils.setTimeout(delayMillis, scheduler)
        .thenCompose(v -> fetchWithRetries(attempt + 1));
  }

  private CompletableFuture<TokenResult> fetchImpl() {
    GetAuthTokenRequest request = GetAuthTokenRequest.newBuilder().setAccessKey(accessKey).build();

    ListenableFuture<GetAuthTokenResponse> future = stub.getAuthToken(request);
    return FutureUtils.toCompletable(future, scheduler)
        .thenApply(resp -> new TokenResult(resp.getAuthToken(), resp.getExpiresInS()));
  }

  /** Closes the token fetcher and cancels any ongoing fetch operations. */
  @Override
  public void close() {
    if (!channel.isShutdown()) {
      // Cancel all ongoing fetch operations before shutting down the channel
      for (CompletableFuture<TokenResult> future : ongoingFetches) {
        future.cancel(true);
      }
      channel.shutdownNow();
    }
  }

  /**
   * Determines if a gRPC error is retryable.
   *
   * @param code The gRPC status code to check
   * @return true if the error is retryable, false otherwise
   */
  private static boolean isRetryableError(Status.Code code) {
    return !NON_RETRYABLE_ERRORS.contains(code);
  }

  /**
   * Calculates backoff duration using exponential backoff with full jitter.
   *
   * @param attempt The current attempt number (0-based)
   * @param baseBackoff The base backoff time in the desired unit
   * @return The duration to wait before retrying, in the same unit as baseBackoff
   */
  private long backoff(int attempt, long baseBackoff) {
    // Cap the exponential multiplier to prevent overflow
    double multiplier = Math.pow(2, Math.min(attempt, MAX_BACKOFF_ATTEMPTS));
    double jitter = ThreadLocalRandom.current().nextDouble();
    return (long) (multiplier * jitter * baseBackoff);
  }
}
