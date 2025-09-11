package cloud.stately.statelydb.auth;

import cloud.stately.statelydb.common.StatelyException;
import java.util.concurrent.CompletableFuture;

/**
 * TokenFetcher is an interface that should be implemented for individual token fetcher
 * implementations.
 */
public interface TokenFetcher extends AutoCloseable {

  /**
   * Fetch a new access token.
   *
   * @return The fetched TokenResult
   * @throws StatelyException If the token cannot be fetched
   */
  CompletableFuture<TokenResult> fetch() throws StatelyException;
}
