package cloud.stately.statelydb.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import cloud.stately.errors.StatelyErrorDetails;
import com.google.protobuf.Any;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import org.junit.jupiter.api.Test;

/** Unit tests for StatelyException. */
class StatelyExceptionTest {

  @Test
  void testBasicConstructor() {
    StatelyException ex =
        new StatelyException("Test message", Status.Code.INVALID_ARGUMENT, "TEST_CODE");

    assertEquals("(InvalidArgument/TEST_CODE): Test message", ex.getMessage());
    assertEquals(Status.Code.INVALID_ARGUMENT, ex.getGrpcCode());
    assertEquals("TEST_CODE", ex.getStatelyCode());
    assertEquals("InvalidArgument", ex.getCodeString());
  }

  @Test
  void testConstructorWithCause() {
    Exception cause = new RuntimeException("Original cause");
    StatelyException ex =
        new StatelyException("Test message", Status.Code.INTERNAL, "INTERNAL_ERROR", cause);

    assertEquals("(Internal/INTERNAL_ERROR): Test message", ex.getMessage());
    assertEquals(Status.Code.INTERNAL, ex.getGrpcCode());
    assertEquals("INTERNAL_ERROR", ex.getStatelyCode());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void testFromExistingStatelyException() {
    StatelyException original =
        new StatelyException("Original", Status.Code.NOT_FOUND, "NOT_FOUND");
    StatelyException converted = StatelyException.from(original);

    assertSame(original, converted);
  }

  @Test
  void testFromGenericException() {
    RuntimeException generic = new RuntimeException("Generic error");
    StatelyException converted = StatelyException.from(generic);

    assertEquals("(Unknown/Unknown): Generic error", converted.getMessage());
    assertEquals(Status.Code.UNKNOWN, converted.getGrpcCode());
    assertEquals("Unknown", converted.getStatelyCode());
    assertEquals(generic, converted.getCause());
  }

  @Test
  void testFromStatusRuntimeException() {
    StatusRuntimeException statusEx =
        Status.PERMISSION_DENIED.withDescription("Access denied").asRuntimeException();

    StatelyException converted = StatelyException.from(statusEx);

    assertEquals("(PermissionDenied/Unknown): Access denied", converted.getMessage());
    assertEquals(Status.Code.PERMISSION_DENIED, converted.getGrpcCode());
    assertEquals("Unknown", converted.getStatelyCode());
    assertEquals(statusEx, converted.getCause());
  }

  @Test
  void testFromStatusRuntimeExceptionWithStatelyErrorDetails() {
    // Create StatelyErrorDetails
    StatelyErrorDetails errorDetails =
        StatelyErrorDetails.newBuilder()
            .setStatelyCode("CUSTOM_ERROR")
            .setMessage("Custom error message")
            .setUpstreamCause("Database connection failed")
            .build();

    // Pack into Any
    Any detailsAny = Any.pack(errorDetails);

    // Create gRPC Status with details
    com.google.rpc.Status grpcStatus =
        com.google.rpc.Status.newBuilder()
            .setCode(Status.Code.UNAVAILABLE.value())
            .setMessage("Service unavailable")
            .addDetails(detailsAny)
            .build();

    // Convert to StatusRuntimeException
    StatusRuntimeException statusEx = StatusProto.toStatusRuntimeException(grpcStatus);

    StatelyException converted = StatelyException.from(statusEx);

    assertEquals("(Unavailable/CUSTOM_ERROR): Custom error message", converted.getMessage());
    assertEquals(Status.Code.UNAVAILABLE, converted.getGrpcCode());
    assertEquals("CUSTOM_ERROR", converted.getStatelyCode());
    assertNotNull(converted.getCause());
    assertEquals("Database connection failed", converted.getCause().getMessage());
  }

  @Test
  void testGrpcCodeToString() {
    assertEquals("Ok", StatelyException.grpcCodeToString(Status.Code.OK));
    assertEquals(
        "InvalidArgument", StatelyException.grpcCodeToString(Status.Code.INVALID_ARGUMENT));
    assertEquals("NotFound", StatelyException.grpcCodeToString(Status.Code.NOT_FOUND));
    assertEquals(
        "PermissionDenied", StatelyException.grpcCodeToString(Status.Code.PERMISSION_DENIED));
    assertEquals("Unknown", StatelyException.grpcCodeToString(null));
  }

  @Test
  void testFromNullException() {
    StatelyException converted = StatelyException.from(new RuntimeException());

    assertEquals("(Unknown/Unknown): RuntimeException", converted.getMessage());
    assertEquals(Status.Code.UNKNOWN, converted.getGrpcCode());
    assertEquals("Unknown", converted.getStatelyCode());
  }
}
