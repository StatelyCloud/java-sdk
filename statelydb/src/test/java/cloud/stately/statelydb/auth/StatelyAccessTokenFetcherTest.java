package cloud.stately.statelydb.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import cloud.stately.auth.GetAuthTokenResponse;
import cloud.stately.statelydb.auth.MockAuthService.GetAuthTokenHandler;
import cloud.stately.statelydb.common.StatelyException;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for StatelyAccessTokenFetcher. */
class StatelyAccessTokenFetcherTest {

  private ScheduledExecutorService scheduler;

  @BeforeEach
  void setUp() {
    scheduler = Executors.newScheduledThreadPool(1);
  }

  @AfterEach
  void tearDown() {
    scheduler.shutdownNow();
  }

  @Test
  void testConstructor() throws Exception {
    StatelyAccessTokenFetcher fetcher =
        StatelyAccessTokenFetcher.builder(new URI("http://localhost:8080"), "test-key", scheduler)
            .baseRetryBackoffSecs(1)
            .build();

    assertNotNull(fetcher);
    fetcher.close();
  }

  @Test
  void testBackoffCalculation() throws Exception {
    StatelyAccessTokenFetcher fetcher =
        StatelyAccessTokenFetcher.builder(new URI("http://localhost:8080"), "test-key", scheduler)
            .baseRetryBackoffSecs(2)
            .build();

    // Use reflection to access private backoff method
    Method backoffMethod =
        StatelyAccessTokenFetcher.class.getDeclaredMethod("backoff", int.class, long.class);
    backoffMethod.setAccessible(true);

    // Test backoff calculation for different attempts
    long backoff0 = (Long) backoffMethod.invoke(fetcher, 0, 1000L);
    long backoff1 = (Long) backoffMethod.invoke(fetcher, 1, 1000L);
    long backoff2 = (Long) backoffMethod.invoke(fetcher, 2, 1000L);

    // Backoff should be between 0 and expected max for each attempt
    assertTrue(backoff0 >= 0 && backoff0 <= 1000); // 2^0 * jitter * 1000
    assertTrue(backoff1 >= 0 && backoff1 <= 2000); // 2^1 * jitter * 1000
    assertTrue(backoff2 >= 0 && backoff2 <= 4000); // 2^2 * jitter * 1000

    fetcher.close();
  }

  @Test
  void testIsRetryableError() throws Exception {
    // Use reflection to access private method
    Method isRetryableMethod =
        StatelyAccessTokenFetcher.class.getDeclaredMethod("isRetryableError", Status.Code.class);
    isRetryableMethod.setAccessible(true);

    // Test non-retryable errors
    assertFalse((Boolean) isRetryableMethod.invoke(null, Status.Code.UNAUTHENTICATED));
    assertFalse((Boolean) isRetryableMethod.invoke(null, Status.Code.PERMISSION_DENIED));
    assertFalse((Boolean) isRetryableMethod.invoke(null, Status.Code.NOT_FOUND));
    assertFalse((Boolean) isRetryableMethod.invoke(null, Status.Code.UNIMPLEMENTED));
    assertFalse((Boolean) isRetryableMethod.invoke(null, Status.Code.INVALID_ARGUMENT));

    // Test retryable errors
    assertTrue((Boolean) isRetryableMethod.invoke(null, Status.Code.UNAVAILABLE));
    assertTrue((Boolean) isRetryableMethod.invoke(null, Status.Code.DEADLINE_EXCEEDED));
    assertTrue((Boolean) isRetryableMethod.invoke(null, Status.Code.INTERNAL));
  }

  @Test
  void testCloseMethod() throws Exception {
    StatelyAccessTokenFetcher fetcher =
        StatelyAccessTokenFetcher.builder(new URI("http://localhost:8080"), "test-key", scheduler)
            .baseRetryBackoffSecs(1)
            .build();

    // This should not throw an exception
    assertDoesNotThrow(() -> fetcher.close());
  }

  @Test
  void testErrorCategorization() throws Exception {
    // Use reflection to test different error categories
    Method isRetryableMethod =
        StatelyAccessTokenFetcher.class.getDeclaredMethod("isRetryableError", Status.Code.class);
    isRetryableMethod.setAccessible(true);

    // All non-retryable errors should return false
    Status.Code[] nonRetryableErrors = {
      Status.Code.UNAUTHENTICATED,
      Status.Code.PERMISSION_DENIED,
      Status.Code.NOT_FOUND,
      Status.Code.UNIMPLEMENTED,
      Status.Code.INVALID_ARGUMENT
    };

    for (Status.Code code : nonRetryableErrors) {
      assertFalse(
          (Boolean) isRetryableMethod.invoke(null, code),
          "Error code " + code + " should not be retryable");
    }

    // All other errors should be retryable
    Status.Code[] retryableErrors = {
      Status.Code.UNAVAILABLE,
      Status.Code.DEADLINE_EXCEEDED,
      Status.Code.INTERNAL,
      Status.Code.RESOURCE_EXHAUSTED,
      Status.Code.ABORTED,
      Status.Code.UNKNOWN
    };

    for (Status.Code code : retryableErrors) {
      assertTrue(
          (Boolean) isRetryableMethod.invoke(null, code),
          "Error code " + code + " should be retryable");
    }
  }

  @Test
  void testFetchWithRealServer_validToken() throws Exception {

    // Create a test server
    MockAuthService testService = new MockAuthService();

    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    try {

      // Test successful token fetch
      StatelyAccessTokenFetcher fetcher =
          StatelyAccessTokenFetcher.builder(
                  new URI("http://localhost:" + serverPort), "valid-key", scheduler)
              .baseRetryBackoffSecs(1)
              .build();

      try {
        CompletableFuture<TokenResult> future = fetcher.fetch();
        TokenResult result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals("test-token-12345", result.getToken());
        assertEquals(3600, result.getExpiresInSecs());
      } finally {
        fetcher.close();
      }

    } finally {
      server.shutdown();
      server.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  @Test
  void testFetchWithRealServer_invalidToken() throws Exception {

    // Create a test server
    MockAuthService testService = new MockAuthService();

    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    try {

      // Test failed token fetch
      StatelyAccessTokenFetcher failingFetcher =
          StatelyAccessTokenFetcher.builder(
                  new URI("http://localhost:" + serverPort), "invalid-key", scheduler)
              .baseRetryBackoffSecs(1)
              .build();

      try {
        CompletableFuture<TokenResult> future = failingFetcher.fetch();
        future.get(5, TimeUnit.SECONDS); // This should throw an exception
        fail("Expected StatelyException due to invalid access key");
      } catch (ExecutionException e) {
        assertTrue(e.getCause() instanceof StatelyException);
        StatelyException se = (StatelyException) e.getCause();
        assertEquals(Status.Code.UNAUTHENTICATED, se.getGrpcCode());
      } finally {
        failingFetcher.close();
      }

    } finally {
      server.shutdown();
      server.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  @Test
  void testFetchWithRealServer_retriesRetryableErrors() throws Exception {
    // Create a test server implementation that simulates retryable errors
    AtomicInteger callCount = new AtomicInteger(0);

    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          int currentCall = callCount.getAndIncrement();
          if (currentCall < 2) {
            responseObserver.onError(
                Status.UNAVAILABLE
                    .withDescription("Service temporarily unavailable")
                    .asRuntimeException());
          } else {
            GetAuthTokenResponse response =
                GetAuthTokenResponse.newBuilder()
                    .setAuthToken("test-token-12345")
                    .setExpiresInS(3600)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
          }
        };

    MockAuthService testService = new MockAuthService(handler);

    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    // Test successful token fetch after retries
    StatelyAccessTokenFetcher fetcher =
        StatelyAccessTokenFetcher.builder(
                new URI("http://localhost:" + serverPort), "valid-key", scheduler)
            .baseRetryBackoffSecs(0)
            .build();

    CompletableFuture<TokenResult> future = fetcher.fetch();
    TokenResult result = future.get(5, TimeUnit.SECONDS);

    assertNotNull(result);
    assertEquals("test-token-12345", result.getToken());
    assertEquals(3600, result.getExpiresInSecs());

    // Verify that the service was called 3 times (2 failures + 1 success)
    assertEquals(3, callCount.get(), "Expected 3 calls: 2 retryable failures + 1 success");

    fetcher.close();

    server.shutdown();
    server.awaitTermination(5, TimeUnit.SECONDS);
  }

  @Test
  void testFetchWithRealServer_skipsNonRetryableErrors() throws Exception {
    AtomicInteger callCount = new AtomicInteger(0);
    // Create a handler that simulates a non-retryable error
    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          callCount.incrementAndGet();
          responseObserver.onError(
              Status.PERMISSION_DENIED.withDescription("Access denied").asRuntimeException());
        };

    MockAuthService testService = new MockAuthService(handler);

    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    // Test failed token fetch with non-retryable error
    StatelyAccessTokenFetcher fetcher =
        StatelyAccessTokenFetcher.builder(
                new URI("http://localhost:" + serverPort), "valid-key", scheduler)
            .baseRetryBackoffSecs(1)
            .build();

    try {
      CompletableFuture<TokenResult> future = fetcher.fetch();
      future.get(5, TimeUnit.SECONDS); // This should throw an exception
      fail("Expected StatelyException due to non-retryable error");
    } catch (ExecutionException e) {
      assertTrue(e.getCause() instanceof StatelyException);
      StatelyException se = (StatelyException) e.getCause();
      assertEquals(Status.Code.PERMISSION_DENIED, se.getGrpcCode());
      assertEquals("(PermissionDenied/Unknown): Access denied", se.getMessage());
      assertEquals(callCount.get(), 1, "Expected only one call to the service");
    } finally {
      fetcher.close();
    }

    server.shutdown();
    server.awaitTermination(5, TimeUnit.SECONDS);
  }

  @Test
  void testFetchWithRealServer_enforcesMaxRetries() throws Exception {
    AtomicInteger callCount = new AtomicInteger(0);
    // Create a handler that always returns retryable errors
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

    // Test failed token fetch with max retries enforced
    StatelyAccessTokenFetcher fetcher =
        StatelyAccessTokenFetcher.builder(
                new URI("http://localhost:" + serverPort), "valid-key", scheduler)
            .baseRetryBackoffSecs(0)
            .build();

    try {
      CompletableFuture<TokenResult> future = fetcher.fetch();
      future.get(5, TimeUnit.SECONDS); // This should throw an exception
      fail("Expected StatelyException due to max retries exceeded");
    } catch (ExecutionException e) {
      assertTrue(e.getCause() instanceof StatelyException);
      StatelyException se = (StatelyException) e.getCause();
      assertEquals(Status.Code.UNAVAILABLE, se.getGrpcCode());
      assertEquals("(Unavailable/Unknown): Service temporarily unavailable", se.getMessage());
      assertEquals(callCount.get(), 11, "Expected initial call + 10 retries");
    } finally {
      fetcher.close();
    }

    server.shutdown();
    server.awaitTermination(5, TimeUnit.SECONDS);
  }

  @Test
  void testFetchWithRealServer_close() throws Exception {
    // Create a handler that returns a successful response
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

    MockAuthService testService = new MockAuthService(handler);

    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    // Test successful token fetch
    StatelyAccessTokenFetcher fetcher =
        StatelyAccessTokenFetcher.builder(
                new URI("http://localhost:" + serverPort), "valid-key", scheduler)
            .baseRetryBackoffSecs(1)
            .build();

    CompletableFuture<TokenResult> future = fetcher.fetch();
    TokenResult result = future.get(5, TimeUnit.SECONDS);

    assertNotNull(result);
    assertEquals("test-token-12345", result.getToken());
    assertEquals(3600, result.getExpiresInSecs());

    fetcher.close();

    server.shutdown();
    server.awaitTermination(5, TimeUnit.SECONDS);
  }

  @Test
  void testFetchWithRealServer_closeRetrying() throws Exception {
    // Create a test server implementation
    AtomicInteger callCount = new AtomicInteger(0);
    CompletableFuture<Void> latch = new CompletableFuture<>();

    GetAuthTokenHandler handler =
        (request, responseObserver) -> {
          if (callCount.incrementAndGet() == 1) {
            responseObserver.onError(
                Status.UNAVAILABLE
                    .withDescription("Service temporarily unavailable")
                    .asRuntimeException());
          } else {
            // second call - signal that we're in retry loop, then block indefinitely
            latch.complete(null);
          }
        };

    MockAuthService testService = new MockAuthService(handler);
    // Start a test server on a random port
    Server server = ServerBuilder.forPort(0).addService(testService).build().start();

    int serverPort = server.getPort();

    // Test closing the fetcher
    StatelyAccessTokenFetcher fetcher =
        StatelyAccessTokenFetcher.builder(
                new URI("http://localhost:" + serverPort), "valid-key", scheduler)
            .baseRetryBackoffSecs(0)
            .build();

    // create a future that will be blocked indefinitely
    CompletableFuture<TokenResult> result = fetcher.fetch();
    // wait until we're in the retry loop
    latch.get(5, TimeUnit.SECONDS);

    // close the fetcher immediately. before the future is completed.
    assertDoesNotThrow(() -> fetcher.close());
    // the future should complete exceptionally
    assertThrows(CancellationException.class, () -> result.get(5, TimeUnit.SECONDS));
    assertEquals(2, callCount.get(), "Expected initial call + 1 retry");

    server.shutdown();
    server.awaitTermination(5, TimeUnit.SECONDS);
  }
}
