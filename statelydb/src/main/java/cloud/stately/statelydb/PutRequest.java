package cloud.stately.statelydb;

import cloud.stately.statelydb.schema.StatelyItem;

/**
 * Request object for putting items into the Stately database.
 *
 * @param <T> the type of item being put
 */
public class PutRequest<T extends StatelyItem> {
  private final T item;
  private boolean mustNotExist = false;
  private boolean overwriteMetadataTimestamp = false;

  private PutRequest(T item) {
    this.item = item;
  }

  /**
   * Gets the item to be put.
   *
   * @return the item
   */
  public T item() {
    return item;
  }

  /**
   * Returns whether the item must not exist for the put to succeed.
   *
   * @return true if the item must not exist
   */
  public boolean mustNotExist() {
    return mustNotExist;
  }

  /**
   * Returns whether to overwrite the metadata timestamp.
   *
   * @return true if the metadata timestamp should be overwritten
   */
  public boolean overwriteMetadataTimestamp() {
    return overwriteMetadataTimestamp;
  }

  /**
   * Creates a new builder for PutRequest.
   *
   * @param <T> the type of item being put
   * @param item the item to be put
   * @return a new Builder instance
   */
  public static <T extends StatelyItem> Builder<T> builder(T item) {
    return new Builder<>(item);
  }

  /**
   * Builder for creating PutRequest instances.
   *
   * @param <T> the type of item being put
   */
  public static class Builder<T extends StatelyItem> {
    private final PutRequest<T> request;

    /**
     * Creates a new builder with the given item. This is not meant to be called directly; use
     * {@link PutRequest#builder(StatelyItem)} instead.
     *
     * @param item the item to be put
     */
    private Builder(T item) {
      this.request = new PutRequest<>(item);
    }

    /**
     * Sets the request to require that the item must not exist.
     *
     * @return this builder for chaining
     */
    public Builder<T> mustNotExist() {
      this.request.mustNotExist = true;
      return this;
    }

    /**
     * Sets the request to overwrite the metadata timestamp.
     *
     * @return this builder for chaining
     */
    public Builder<T> overwriteMetadataTimestamp() {
      this.request.overwriteMetadataTimestamp = true;
      return this;
    }

    /**
     * Builds the PutRequest.
     *
     * @return the configured PutRequest
     */
    public PutRequest<T> build() {
      return this.request;
    }
  }
}
