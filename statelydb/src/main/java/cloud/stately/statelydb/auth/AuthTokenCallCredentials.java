package cloud.stately.statelydb.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import java.util.concurrent.Executor;

/**
 * A CallCredentials implementation that attaches a JWT bearer token to each call. It uses a
 * TokenProvider to asynchronously fetch the token.
 */
public class AuthTokenCallCredentials extends CallCredentials {

  private final TokenProvider tokenProvider;

  /**
   * Creates new AuthTokenCallCredentials with the given token provider.
   *
   * @param tokenProvider the token provider to use for authentication
   */
  public AuthTokenCallCredentials(TokenProvider tokenProvider) {
    if (tokenProvider == null) {
      throw new IllegalArgumentException("TokenProvider cannot be null");
    }
    this.tokenProvider = tokenProvider;
  }

  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    // Use the provided executor to run the async token fetch
    appExecutor.execute(
        () -> {
          tokenProvider
              .getToken(false)
              .whenComplete(
                  (token, error) -> {
                    if (error != null) {
                      // If token fetch fails, fail the call with UNAUTHENTICATED
                      applier.fail(
                          Status.UNAUTHENTICATED
                              .withDescription("Failed to get authentication token")
                              .withCause(error));
                      return;
                    }

                    // If token fetch succeeds, apply the header
                    Metadata headers = new Metadata();
                    Metadata.Key<String> authHeader =
                        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
                    headers.put(authHeader, "Bearer " + token);
                    applier.apply(headers);
                  });
        });
  }

  @Override
  public void thisUsesUnstableApi() {
    // Required implementation for CallCredentials interface
  }
}
