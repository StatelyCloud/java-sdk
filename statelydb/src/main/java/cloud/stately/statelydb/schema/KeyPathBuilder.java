package cloud.stately.statelydb.schema;

import java.util.Base64;
import java.util.UUID;

/**
 * Builder for constructing Stately key paths. Provides methods to build hierarchical key paths with
 * namespaces and identifiers.
 */
public class KeyPathBuilder {
  private final StringBuilder keyPath;

  /** Creates a new KeyPathBuilder with an empty key path. */
  public KeyPathBuilder() {
    this.keyPath = new StringBuilder();
  }

  /**
   * Adds a namespace segment to the key path.
   *
   * @param namespace the namespace to add
   * @return this builder for chaining
   */
  public KeyPathBuilder with(String namespace) {
    this.keyPath.append("/").append(namespace);
    return this;
  }

  /**
   * Adds a namespace segment with a UUID identifier to the key path.
   *
   * @param namespace the namespace to add
   * @param id the UUID identifier
   * @return this builder for chaining
   */
  public KeyPathBuilder with(String namespace, UUID id) {
    return with(namespace, id.toString());
  }

  /**
   * Adds a namespace segment with a string identifier to the key path.
   *
   * @param namespace the namespace to add
   * @param id the string identifier
   * @return this builder for chaining
   */
  public KeyPathBuilder with(String namespace, String id) {
    this.keyPath.append("/").append(namespace).append("-").append(id.replace("/", "%/"));
    return this;
  }

  /**
   * Adds a namespace segment with a byte array identifier to the key path.
   *
   * @param namespace the namespace to add
   * @param id the byte array identifier
   * @return this builder for chaining
   */
  public KeyPathBuilder with(String namespace, byte[] id) {
    this.keyPath
        .append("/")
        .append(namespace)
        .append("-")
        .append(Base64.getUrlEncoder().withoutPadding().encodeToString(id));
    return this;
  }

  /**
   * Adds a namespace segment with an int identifier to the key path.
   *
   * @param namespace the namespace to add
   * @param id the int identifier
   * @return this builder for chaining
   */
  public KeyPathBuilder with(String namespace, int id) {
    this.keyPath.append("/").append(namespace).append("-").append(id);
    return this;
  }

  /**
   * Adds a namespace segment with a long identifier to the key path.
   *
   * @param namespace the namespace to add
   * @param id the long identifier
   * @return this builder for chaining
   */
  public KeyPathBuilder with(String namespace, long id) {
    this.keyPath.append("/").append(namespace).append("-").append(id);
    return this;
  }

  /**
   * Adds a namespace segment with an enum identifier to the key path.
   *
   * @param namespace the namespace to add
   * @param id the enum identifier
   * @return this builder for chaining
   */
  public KeyPathBuilder with(String namespace, Enum<?> id) {
    return with(namespace, id.ordinal());
  }

  /**
   * Adds a namespace segment with a StatelyEnum identifier to the key path.
   *
   * @param namespace the namespace to add
   * @param id the StatelyEnum identifier
   * @return this builder for chaining
   */
  public KeyPathBuilder with(String namespace, StatelyEnum id) {
    return with(namespace, id.getNumber());
  }

  /**
   * Builds the final key path string.
   *
   * @return the constructed key path
   */
  public String build() {
    if (keyPath.length() == 0) {
      return "/";
    }
    return keyPath.toString();
  }
}
