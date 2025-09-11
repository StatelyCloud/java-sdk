package cloud.stately.statelydb.common;

import cloud.stately.errors.StatelyErrorDetails;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;

/**
 * StatelyException represents an error that occurred during StatelyDB operations. This is the main
 * exception type used throughout the Java SDK.
 */
public class StatelyException extends RuntimeException {

  private static final String ERROR_DETAILS_TYPE_URL =
      "type.googleapis.com/stately.errors.StatelyErrorDetails";

  /** The gRPC status code for this exception. */
  private final Status.Code grpcCode;

  /** The Stately-specific error code for this exception. */
  private final String statelyCode;

  /**
   * Creates a new StatelyException.
   *
   * @param message The error message
   * @param grpcCode The gRPC status code
   * @param statelyCode The Stately-specific error code
   */
  public StatelyException(String message, Status.Code grpcCode, String statelyCode) {
    super(formatMessage(message, grpcCode, statelyCode));
    this.grpcCode = grpcCode;
    this.statelyCode = statelyCode;
  }

  /**
   * Creates a new StatelyException with a cause.
   *
   * @param message The error message
   * @param grpcCode The gRPC status code
   * @param statelyCode The Stately-specific error code
   * @param cause The underlying cause
   */
  public StatelyException(
      String message, Status.Code grpcCode, String statelyCode, Throwable cause) {
    super(formatMessage(message, grpcCode, statelyCode), cause);
    this.grpcCode = grpcCode;
    this.statelyCode = statelyCode;
  }

  /**
   * Convert any exception into a StatelyException. This method handles the conversion of gRPC
   * status exceptions with StatelyErrorDetails into user-friendly StatelyException instances.
   *
   * @param exception The exception to convert
   * @return A StatelyException representing the error
   */
  public static StatelyException from(Throwable exception) {
    Throwable cause = (exception.getCause() != null) ? exception.getCause() : exception;
    // If it's already a StatelyException, return as-is
    if (cause instanceof StatelyException) {
      return (StatelyException) cause;
    }

    // Handle gRPC StatusRuntimeException
    if (cause instanceof StatusRuntimeException) {
      StatusRuntimeException statusException = (StatusRuntimeException) cause;

      // Try to extract StatelyErrorDetails from the status
      com.google.rpc.Status status = StatusProto.fromThrowable(statusException);
      if (status != null && !status.getDetailsList().isEmpty()) {
        for (Any detail : status.getDetailsList()) {
          if (ERROR_DETAILS_TYPE_URL.equals(detail.getTypeUrl())) {
            try {
              StatelyErrorDetails errorDetails = detail.unpack(StatelyErrorDetails.class);

              // Create upstream cause if available
              Throwable upstreamCause = null;
              if (!errorDetails.getUpstreamCause().isEmpty()) {
                upstreamCause = new Exception(errorDetails.getUpstreamCause());
              }

              return new StatelyException(
                  errorDetails.getMessage(),
                  statusException.getStatus().getCode(),
                  errorDetails.getStatelyCode(),
                  upstreamCause);
            } catch (InvalidProtocolBufferException e) {
              // If we can't parse the details, fall through to generic handling
              break;
            }
          }
        }
      }

      // Fallback for StatusRuntimeException without StatelyErrorDetails
      return new StatelyException(
          statusException.getStatus().getDescription(),
          statusException.getStatus().getCode(),
          "Unknown",
          statusException);
    }

    // Fallback for any other exception
    return new StatelyException(
        exception.getMessage() != null
            ? exception.getMessage()
            : exception.getClass().getSimpleName(),
        Status.Code.UNKNOWN,
        "Unknown",
        exception);
  }

  /**
   * Get the gRPC status code.
   *
   * @return The gRPC status code
   */
  public Status.Code getGrpcCode() {
    return grpcCode;
  }

  /**
   * Get the Stately-specific error code.
   *
   * @return The Stately-specific error code
   */
  public String getStatelyCode() {
    return statelyCode;
  }

  /**
   * Get the gRPC status code as a human-readable string.
   *
   * @return The gRPC status code as a string
   */
  public String getCodeString() {
    return grpcCodeToString(grpcCode);
  }

  /**
   * Convert a gRPC status code to a human-readable string.
   *
   * @param code The gRPC status code
   * @return The status code as a human-readable string
   */
  public static String grpcCodeToString(Status.Code code) {
    if (code == null) {
      return "Unknown";
    }

    // Convert enum name from UPPER_CASE to PascalCase
    String name = code.name();
    if (name.equals("OK")) {
      return "Ok";
    }

    StringBuilder result = new StringBuilder();
    boolean capitalizeNext = true;

    for (char c : name.toCharArray()) {
      if (c == '_') {
        capitalizeNext = true;
      } else if (capitalizeNext) {
        result.append(Character.toUpperCase(c));
        capitalizeNext = false;
      } else {
        result.append(Character.toLowerCase(c));
      }
    }

    return result.toString();
  }

  /**
   * Format the error message in the standard StatelyDB format.
   *
   * @param message The error message
   * @param grpcCode The gRPC status code
   * @param statelyCode The Stately-specific error code
   * @return The formatted message
   */
  private static String formatMessage(String message, Status.Code grpcCode, String statelyCode) {
    String codeStr = grpcCodeToString(grpcCode);
    return String.format("(%s/%s): %s", codeStr, statelyCode, message);
  }
}
