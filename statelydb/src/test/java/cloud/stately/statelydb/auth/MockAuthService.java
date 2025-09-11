package cloud.stately.statelydb.auth;

import cloud.stately.auth.AuthServiceGrpc;
import cloud.stately.auth.GetAuthTokenRequest;
import cloud.stately.auth.GetAuthTokenResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/** Mock AuthService that accepts a handler for testing. */
public class MockAuthService extends AuthServiceGrpc.AuthServiceImplBase {
  /** Functional interface for handling GetAuthToken requests. */
  @FunctionalInterface
  public interface GetAuthTokenHandler {
    /**
     * Handles a GetAuthToken request.
     *
     * @param request the GetAuthTokenRequest
     * @param responseObserver the StreamObserver for GetAuthTokenResponse
     */
    void handle(GetAuthTokenRequest request, StreamObserver<GetAuthTokenResponse> responseObserver);
  }

  private final GetAuthTokenHandler handler;

  /**
   * Constructs a MockAuthService with the specified handler.
   *
   * @param handler the handler for GetAuthToken requests
   */
  public MockAuthService(GetAuthTokenHandler handler) {
    this.handler = handler;
  }

  /**
   * Constructs a MockAuthService with a default handler that accepts "valid-key" as the access key.
   */
  public MockAuthService() {
    this(
        (request, responseObserver) -> {
          if ("valid-key".equals(request.getAccessKey())) {
            GetAuthTokenResponse response =
                GetAuthTokenResponse.newBuilder()
                    .setAuthToken("test-token-12345")
                    .setExpiresInS(3600)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
          } else {
            responseObserver.onError(
                Status.UNAUTHENTICATED.withDescription("Invalid access key").asRuntimeException());
          }
        });
  }

  @Override
  public void getAuthToken(
      GetAuthTokenRequest request, StreamObserver<GetAuthTokenResponse> responseObserver) {
    handler.handle(request, responseObserver);
  }
}
