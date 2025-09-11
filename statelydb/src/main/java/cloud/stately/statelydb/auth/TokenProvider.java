package cloud.stately.statelydb.auth;

import java.util.concurrent.CompletableFuture;

/**
 * TokenProvider is an interface that should be implemented for individual token provider
 * implementations. It provides access tokens for authentication with StatelyDB services.
 */
public interface TokenProvider extends AutoCloseable {

  /**
   * Get the current access token.
   *
   * @param force Whether to force a refresh of the token
   * @return A CompletableFuture containing the current access token
   */
  CompletableFuture<String> getToken(boolean force);

  /**
   * Get the current access token without forcing a refresh.
   *
   * @return A CompletableFuture containing the current access token
   */
  default CompletableFuture<String> getToken() {
    return getToken(false);
  }
}
