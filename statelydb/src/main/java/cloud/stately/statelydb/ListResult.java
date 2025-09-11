package cloud.stately.statelydb;

import cloud.stately.db.ListToken;
import cloud.stately.statelydb.schema.StatelyItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a list operation, containing items and a token for pagination. This
 * class provides access to the items returned from a list operation and the token needed for
 * pagination or sync operations.
 */
public class ListResult {
  private final List<StatelyItem> items;
  private final ListToken token;

  /**
   * Creates a new ListResult with the provided items and token.
   *
   * @param items the list of StatelyItems returned from the operation
   * @param token the token for pagination or sync operations
   */
  public ListResult(List<StatelyItem> items, ListToken token) {
    this.items = new ArrayList<>(items);
    this.token = token;
  }

  /**
   * Returns the token from this list operation. This token can be used with continueList to get
   * more results or syncList to get updates.
   *
   * @return The ListToken
   */
  public ListToken getToken() {
    return token;
  }

  /**
   * Returns the items from this list operation.
   *
   * @return The list of StatelyItems
   */
  public List<StatelyItem> getItems() {
    return items;
  }
}
