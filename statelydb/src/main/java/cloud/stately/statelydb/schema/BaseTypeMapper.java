package cloud.stately.statelydb.schema;

import cloud.stately.db.Item;

/**
 * Abstract base class that all generated StatelyDB schemas extend. Provides common functionality
 * for schema identification and type mapping.
 */
public abstract class BaseTypeMapper {
  private final long schemaId;
  private final int schemaVersionId;

  /**
   * Creates a new BaseTypeMapper with the given schema information.
   *
   * @param schemaVersionId the schema version ID
   * @param schemaId the schema ID
   */
  public BaseTypeMapper(int schemaVersionId, long schemaId) {
    this.schemaId = schemaId;
    this.schemaVersionId = schemaVersionId;
  }

  /**
   * Returns the schema ID for this type mapper.
   *
   * @return the schema ID
   */
  public long getSchemaId() {
    return schemaId;
  }

  /**
   * Returns the schema version ID for this type mapper.
   *
   * @return the schema version ID
   */
  public int getSchemaVersionId() {
    return schemaVersionId;
  }

  /**
   * Unmarshals a protobuf Item to a StatelyItem.
   *
   * @param pbItem the protobuf Item to unmarshal
   * @return the unmarshalled StatelyItem
   */
  public abstract StatelyItem unmarshal(Item pbItem);
}
