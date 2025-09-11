package cloud.stately.statelydb;

import cloud.stately.db.GeneratedID;
import cloud.stately.statelydb.common.StatelyException;
import io.grpc.Status;

/**
 * Represents a generated identifier that can be either a unsigned long integer or a byte array.
 * This class is used to handle different types of auto-generated IDs in Stately.
 */
public class GeneratedId {
  private final long uint;
  private final byte[] bytes;

  /**
   * Creates a new GeneratedId instance.
   *
   * @param uint the unsigned integer value (must be 0 if bytes is provided)
   * @param bytes the byte array value (must be null if uint is provided)
   * @throws StatelyException if both parameters are null or both are non-null
   */
  private GeneratedId(long uint, byte[] bytes) {
    if ((uint == 0 && bytes == null) || (uint != 0 && bytes != null)) {
      throw new StatelyException(
          "Exactly one of uint or bytes must be non-null",
          Status.Code.INVALID_ARGUMENT,
          "InvalidArgument");
    }
    this.uint = uint;
    this.bytes = bytes;
  }

  /**
   * Gets the unsigned integer value of this ID.
   *
   * @return the unsigned integer value, or 0 if this ID represents bytes
   */
  public long getUint() {
    return uint;
  }

  /**
   * Gets the byte array value of this ID.
   *
   * @return the byte array value, or null if this ID represents an unsigned integer
   */
  public byte[] getBytes() {
    return bytes;
  }

  /**
   * Checks if this ID represents an unsigned integer.
   *
   * @return true if this ID is an unsigned integer, false if it's a byte array
   */
  public boolean isUint() {
    return bytes == null;
  }

  /**
   * Checks if this ID represents a byte array.
   *
   * @return true if this ID is a byte array, false if it's an unsigned integer
   */
  public boolean isBytes() {
    return bytes != null;
  }

  /**
   * Creates a GeneratedId from a protobuf GeneratedID. If the proto has neither uint or bytes, this
   * will return null which is consistent with the API contract enforced elsewhere. This happens
   * when the item already has a real ID assigned.
   *
   * @param protoId the protobuf GeneratedID to convert
   * @return a new GeneratedId instance, or null if the proto is empty
   */
  static GeneratedId fromProto(GeneratedID protoId) {
    if (protoId.hasUint()) {
      return new GeneratedId(protoId.getUint(), null);
    } else if (protoId.hasBytes()) {
      return new GeneratedId(0, protoId.getBytes().toByteArray());
    } else {
      return null;
    }
  }
}
