package cloud.stately.statelydb.schema;

import com.google.protobuf.Descriptors;
import java.util.Objects;

/**
 * Base class for open enums that support undefined values for forwards compatibility.
 *
 * @param <T> the enum type extending StatelyEnum
 */
public abstract class StatelyEnum<T extends StatelyEnum<T>> {

  private final int value;
  private final String name;

  /**
   * Constructor for enum values.
   *
   * @param value the numeric value
   * @param name the name of the enum
   */
  protected StatelyEnum(int value, String name) {
    this.value = value;
    this.name = name;
  }

  /**
   * Get the numeric value of this enum.
   *
   * @return the numeric value
   */
  public int getNumber() {
    return value;
  }

  /**
   * Returns a map of known enum values keyed by their numeric value.
   *
   * @return a map of known enum values
   */
  protected abstract java.util.Map<Integer, T> getKnownValuesMap();

  /**
   * Check if this is a known enum value.
   *
   * @return true if this is a known enum value, false otherwise
   */
  public boolean isKnown() {
    return getKnownValuesMap().containsKey(this.getNumber());
  }

  @Override
  public String toString() {
    if (isKnown()) {
      return name;
    }
    return "UnknownEnumValue_(" + value + ")";
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof StatelyEnum && other.getClass() == getClass()) {
      return this.value == ((StatelyEnum<?>) other).getNumber();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), value);
  }

  /**
   * Marshal this enum to a protobuf descriptor.
   *
   * @return the protobuf enum value descriptor
   */
  public abstract Descriptors.EnumValueDescriptor marshal();
}
