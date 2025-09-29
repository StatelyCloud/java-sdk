package cloud.stately.statelydb.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.stately.auth.GetAuthTokenResponse;
import cloud.stately.statelydb.auth.MockAuthService.GetAuthTokenHandler;
import cloud.stately.statelydb.common.StatelyException;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for AuthTokenProvider. */
class AuthTokenProviderTest {

  private ScheduledExecutorService scheduler;

  @BeforeEach
  void setUp() {
    scheduler = Executors.newScheduledThreadPool(1);
  }

  @AfterEach
  void tearDown() {
    scheduler.shutdownNow();
  }

  /** Helper method to safely set and restore environment variables. */
  private void withEnvironmentVariable(String key, String value, Runnable test) {
    String originalValue = System.getProperty(key);
    try {
      if (value != null) {
        System.setProperty(key, value);
      } else {
        System.clearProperty(key);
      }
      test.run();
    } finally {
      if (originalValue != null) {
        System.setProperty(key, originalValue);
      } else {
        System.clearProperty(key);
      }
    }
  }

  /** Helper method to create a test server with proper cleanup. */
  private void withTestServer(GetAuthTokenHandler handler, ServerConsumer test) throws Exception {
    MockAuthService testService = new MockAuthService(handler);
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();
    test.accept(server.getPort());
    server.shutdownNow();
  }

  @FunctionalInterface
  private interface ServerConsumer {
    void accept(int port) throws Exception;
  }

  @Test
  void testConstructorWithExplicitAccessKey() {
    // Test the existing constructor that takes an explicit access key
    String accessKey = "test-access-key";
    long baseRetryBackoffSecs = 1;

    AuthTokenProvider provider =
        AuthTokenProvider.builder(scheduler)
            .accessKey(accessKey)
            .baseRetryBackoffSecs(baseRetryBackoffSecs)
            .build();

    assertNotNull(provider);
  }

  @Test
  void testConstructorWithEnvironmentVariable() {
    withEnvironmentVariable(
        "STATELY_ACCESS_KEY",
        "env-access-key",
        () -> {
          long baseRetryBackoffSecs = 1;
          AuthTokenProvider provider =
              AuthTokenProvider.builder(scheduler)
                  .baseRetryBackoffSecs(baseRetryBackoffSecs)
                  .build();
          assertNotNull(provider);
        });
  }

  @Test
  void testConstructorWithSchedulerOnly() {
    withEnvironmentVariable(
        "STATELY_ACCESS_KEY",
        "env-access-key",
        () -> {
          AuthTokenProvider provider = AuthTokenProvider.builder(scheduler).build();
          assertNotNull(provider);
        });
  }

  @Test
  void testConstructorWithMissingEnvironmentVariable() {
    withEnvironmentVariable(
        "STATELY_ACCESS_KEY",
        null,
        () -> {
          long baseRetryBackoffSecs = 1;

          StatelyException exception =
              assertThrows(
                  StatelyException.class,
                  () ->
                      AuthTokenProvider.builder(scheduler)
                          .baseRetryBackoffSecs(baseRetryBackoffSecs)
                          .build());

          assertEquals(
              "(Unauthenticated/Unauthenticated): Unable to find an access key in the "
                  + "STATELY_ACCESS_KEY environment variable. Either pass your credentials in the "
                  + "options when creating a client or set this environment variable.",
              exception.getMessage());
        });
  }

  @Test
  void testConstructorWithEmptyEnvironmentVariable() {
    withEnvironmentVariable(
        "STATELY_ACCESS_KEY",
        "",
        () -> {
          long baseRetryBackoffSecs = 1;

          StatelyException exception =
              assertThrows(
                  StatelyException.class,
                  () ->
                      AuthTokenProvider.builder(scheduler)
                          .baseRetryBackoffSecs(baseRetryBackoffSecs)
                          .build());

          assertEquals(
              "(Unauthenticated/Unauthenticated): Unable to find an access key in the "
                  + "STATELY_ACCESS_KEY environment variable. Either pass your credentials in the "
                  + "options when creating a client or set this environment variable.",
              exception.getMessage());
        });
  }

  @Test
  void testConstructorWithWhitespaceOnlyEnvironmentVariable() {
    withEnvironmentVariable(
        "STATELY_ACCESS_KEY",
        "   ",
        () -> {
          long baseRetryBackoffSecs = 1;

          StatelyException exception =
              assertThrows(
                  StatelyException.class,
                  () ->
                      AuthTokenProvider.builder(scheduler)
                          .baseRetryBackoffSecs(baseRetryBackoffSecs)
                          .build());

          assertEquals(
              "(Unauthenticated/Unauthenticated): Unable to find an access key in the "
                  + "STATELY_ACCESS_KEY environment variable. Either pass your credentials in the "
                  + "options when creating a client or set this environment variable.",
              exception.getMessage());
        });
  }

  @Test
  void testTokenFetch() throws Exception {
    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          GetAuthTokenResponse response =
              GetAuthTokenResponse.newBuilder()
                  .setAuthToken("test-token-12345")
                  .setExpiresInS(3600)
                  .build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        };

    withTestServer(
        handler,
        serverPort -> {
          URI endpoint = new URI("http://localhost:" + serverPort);
          AuthTokenProvider provider =
              AuthTokenProvider.builder(scheduler)
                  .accessKey("valid-key")
                  .baseRetryBackoffSecs(1L)
                  .build();
          provider.start(endpoint);
          String token = provider.getToken().get();
          assertEquals("test-token-12345", token);
        });
  }

  @Test
  void testTokenRefresh() throws Exception {
    CompletableFuture<Void> latch = new CompletableFuture<>();
    AtomicInteger callCount = new AtomicInteger(0);
    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          if (callCount.get() == 2) {
            latch.complete(null);
          }
          GetAuthTokenResponse response =
              GetAuthTokenResponse.newBuilder()
                  .setAuthToken(String.valueOf(callCount.incrementAndGet()))
                  .setExpiresInS(1)
                  .build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        };

    withTestServer(
        handler,
        serverPort -> {
          URI endpoint = new URI("http://localhost:" + serverPort);
          AuthTokenProvider provider =
              AuthTokenProvider.builder(scheduler)
                  .accessKey("valid-key")
                  .baseRetryBackoffSecs(1L)
                  .build();
          provider.start(endpoint);
          String token = provider.getToken().get();
          assertEquals("1", token);
          // Wait for the second call to refresh the token
          latch.get();
          token = provider.getToken().get();
          assertEquals("2", token);
          provider.close();
        });
  }

  @Test
  void testNetworkError() throws Exception {
    AtomicInteger callCount = new AtomicInteger(0);
    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          callCount.incrementAndGet();
          responseObserver.onError(
              Status.UNAVAILABLE
                  .withDescription("Service temporarily unavailable")
                  .asRuntimeException());
        };

    MockAuthService testService = new MockAuthService(handler);

    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    URI endpoint = new URI("http://localhost:" + serverPort);
    AuthTokenProvider provider =
        AuthTokenProvider.builder(scheduler)
            .accessKey("valid-key")
            .baseRetryBackoffSecs(0L)
            .build();
    provider.start(endpoint);
    try {
      provider.getToken().get();
    } catch (Exception e) {
      assertTrue(e.getCause() instanceof StatelyException);
      StatelyException se = (StatelyException) e.getCause();
      assertEquals(Status.Code.UNAVAILABLE, se.getGrpcCode());
      assertEquals("(Unavailable/Unknown): Service temporarily unavailable", se.getMessage());
      assertEquals(callCount.get(), 11, "Expected 11 attempts due to network error");
    }
    provider.close();

    server.shutdownNow();
  }

  @Test
  void testDedupesConcurrentRequests() throws Exception {
    AtomicInteger callCount = new AtomicInteger(0);
    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          callCount.incrementAndGet();
          GetAuthTokenResponse response =
              GetAuthTokenResponse.newBuilder()
                  .setAuthToken("test-token-12345")
                  .setExpiresInS(3600)
                  .build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        };

    MockAuthService testService = new MockAuthService(handler);

    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    URI endpoint = new URI("http://localhost:" + serverPort);
    AuthTokenProvider provider =
        AuthTokenProvider.builder(scheduler)
            .accessKey("valid-key")
            .baseRetryBackoffSecs(1L)
            .build();

    provider.start(endpoint);

    List<CompletableFuture<String>> futures = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      futures.add(provider.getToken());
    }
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
    for (CompletableFuture<String> tokenFuture : futures) {
      String token = tokenFuture.get();
      assertEquals(
          token, "test-token-12345", "All concurrent requests should return the same token");
    }
    assertEquals(callCount.get(), 1, "Should only call the server once for concurrent requests");
    provider.close();

    server.shutdownNow();
  }

  @Test
  void testExpiryOverrideOnForce() throws Exception {
    AtomicInteger callCount = new AtomicInteger(0);
    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          GetAuthTokenResponse response =
              GetAuthTokenResponse.newBuilder()
                  .setAuthToken(String.valueOf(callCount.incrementAndGet()))
                  .setExpiresInS(3600)
                  .build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        };

    MockAuthService testService = new MockAuthService(handler);
    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    URI endpoint = new URI("http://localhost:" + serverPort);
    AuthTokenProvider provider =
        AuthTokenProvider.builder(scheduler)
            .accessKey("valid-key")
            .baseRetryBackoffSecs(1L)
            .build();
    provider.start(endpoint);
    // First fetch should succeed
    String token = provider.getToken().get();
    assertEquals(token, "1");

    // second one should return cached token
    token = provider.getToken().get();
    assertEquals(token, "1");

    // Force a refresh
    token = provider.getToken(true).get();
    assertEquals(token, "2");

    provider.close();

    server.shutdownNow();
  }

  @Test
  void testBlockingAfterForce() throws Exception {
    AtomicInteger callCount = new AtomicInteger(0);
    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          if (callCount.get() == 0) {
            GetAuthTokenResponse response =
                GetAuthTokenResponse.newBuilder()
                    .setAuthToken(String.valueOf(callCount.incrementAndGet()))
                    .setExpiresInS(3600)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
          } else {
            // do nothing and block indefinitely
          }
        };

    MockAuthService testService = new MockAuthService(handler);
    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    URI endpoint = new URI("http://localhost:" + serverPort);
    AuthTokenProvider provider =
        AuthTokenProvider.builder(scheduler)
            .accessKey("valid-key")
            .baseRetryBackoffSecs(1L)
            .build();
    provider.start(endpoint);
    // First fetch should succeed
    String token = provider.getToken().get();
    assertEquals(token, "1");

    // second one should return cached token
    token = provider.getToken().get();
    assertEquals(token, "1");

    // Force a refresh. this should block indefinitely.
    CompletableFuture<String> promise1 = provider.getToken(true);
    // getting a non-force token should also block indefinitely
    CompletableFuture<String> promise2 = provider.getToken();

    // verify that both promises time out
    assertThrows(
        TimeoutException.class,
        () -> CompletableFuture.anyOf(promise1, promise2).get(1000, TimeUnit.MILLISECONDS));
    provider.close();

    server.shutdownNow();
  }
}
