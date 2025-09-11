package cloud.stately.statelydb.schema;

import cloud.stately.db.Item;

/**
 * Abstract base class for all generated Stately item types. Provides common functionality for
 * marshalling, cloning, and key path generation.
 */
public abstract class StatelyItem {

  /** Protected constructor for subclasses. */
  protected StatelyItem() {}

  /**
   * Marshals this item to a protobuf Item.
   *
   * @return the marshalled Item
   */
  public abstract Item marshal();

  /**
   * Creates a clone of this item.
   *
   * @return a cloned copy of this item
   */
  public abstract StatelyItem clone();

  /**
   * Gets the primary key path for this item.
   *
   * @return the primary key path as a string
   */
  public abstract String primaryKeyPath();
}
