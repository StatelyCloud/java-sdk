package cloud.stately.statelydb;

import cloud.stately.statelydb.schema.StatelyItem;
import java.util.List;

/**
 * After a transaction is done, this result contains the updated or created items from any puts in
 * the transaction.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * TransactionResult result = client.transaction(txn -> {
 *     Equipment lightsaber = Equipment.builder()
 *         .setColor("green")
 *         .setJedi("luke")
 *         .setType("lightsaber")
 *         .build();
 *     return txn.put(lightsaber);
 * }).get();
 *
 * if (result.isCommitted()) {
 *     System.out.println("Transaction committed with " + result.getPuts().size() + " items");
 *     for (StatelyItem item : result.getPuts()) {
 *         System.out.println("Put item: " + item.primaryKeyPath());
 *     }
 * }
 * }</pre>
 */
public class TransactionResult {
  private final List<StatelyItem> puts;
  private final boolean committed;

  /**
   * Creates a new TransactionResult.
   *
   * @param puts the list of items that were put during the transaction
   * @param committed whether the transaction was committed successfully
   */
  public TransactionResult(List<StatelyItem> puts, boolean committed) {
    this.puts = puts;
    this.committed = committed;
  }

  /**
   * Contains the full result of each Put operation as a new Item. These are the complete items as
   * they exist after the transaction commit, with all metadata fields populated.
   *
   * @return the list of put items
   */
  public List<StatelyItem> getPuts() {
    return puts;
  }

  /**
   * Did the commit finish (the alternative is that it was aborted/rolled back).
   *
   * @return true if the transaction was committed, false if it was aborted
   */
  public boolean isCommitted() {
    return committed;
  }
}
