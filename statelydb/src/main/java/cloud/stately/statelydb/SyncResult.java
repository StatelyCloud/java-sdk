package cloud.stately.statelydb;

import cloud.stately.db.ListToken;
import cloud.stately.statelydb.schema.StatelyItem;
import java.util.List;

/**
 * SyncResult represents the result of a syncList operation, containing information about changes to
 * items within a list result set.
 */
public class SyncResult {
  private final List<StatelyItem> changedItems;
  private final List<String> deletedItemPaths;
  private final List<String> updatedOutsideListWindowPaths;
  private final ListToken token;
  private final boolean isReset;

  /**
   * Creates a new SyncResult.
   *
   * @param changedItems Items that were modified or newly created since the last sync
   * @param deletedItemPaths Key paths of items that were deleted since the last sync
   * @param updatedOutsideListWindowPaths Key paths of items that were updated but are no longer
   *     within the list window
   * @param isReset Whether this is a reset signal indicating cached data should be discarded
   * @param token The token for the next sync operation
   */
  public SyncResult(
      List<StatelyItem> changedItems,
      List<String> deletedItemPaths,
      List<String> updatedOutsideListWindowPaths,
      boolean isReset,
      ListToken token) {
    this.changedItems = changedItems;
    this.deletedItemPaths = deletedItemPaths;
    this.updatedOutsideListWindowPaths = updatedOutsideListWindowPaths;
    this.isReset = isReset;
    this.token = token;
  }

  /**
   * Returns the list of items that were changed or added since the last syncList call.
   *
   * @return List of StatelyItems that have changed
   */
  public List<StatelyItem> getChangedItems() {
    return changedItems;
  }

  /**
   * Returns the list of key paths for items that were deleted since the last syncList call.
   *
   * @return List of key paths for deleted items
   */
  public List<String> getDeletedItemPaths() {
    return deletedItemPaths;
  }

  /**
   * Returns the list of key paths for items that were updated but are no longer within the current
   * result set. You can treat these like deleted items, but they haven't actually been deleted -
   * they're just not part of your view of the list anymore.
   *
   * @return List of key paths for items updated outside the list window
   */
  public List<String> getUpdatedOutsideListWindowPaths() {
    return updatedOutsideListWindowPaths;
  }

  /**
   * Returns whether this is a reset signal. If true, any previously cached view of the result set
   * is no longer valid and should be discarded. This will always be followed by a series of changed
   * items that make up a new view of the result set.
   *
   * @return true if this is a reset signal
   */
  public boolean isReset() {
    return isReset;
  }

  /**
   * Returns the token for the next sync operation.
   *
   * @return The ListToken for continuing sync operations
   */
  public ListToken getToken() {
    return token;
  }
}
