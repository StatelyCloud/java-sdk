package cloud.stately.statelydb;

import cloud.stately.db.ListToken;
import cloud.stately.statelydb.schema.StatelyItem;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Transaction interface that provides data access methods for transactional operations. This
 * interface exposes only the data APIs (get, put, delete, list operations) and does not include
 * transaction control methods like commit() and abort().
 *
 * <p>This object should only be referenced from within a transaction handler function. The normal
 * data functions should not be used within a transaction handler function.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * TransactionResult result = client.transaction(txn -> {
 *     // Get an existing item
 *     return txn.get("/jedi-luke/equipment-lightsaber")
 *         .thenCompose(existingItem -> {
 *             if (existingItem != null && existingItem instanceof Equipment) {
 *                 Equipment equipment = (Equipment) existingItem;
 *                 // Update the item
 *                 equipment.setColor("blue");
 *                 return txn.put(equipment);
 *             } else {
 *                 // Create new item
 *                 Equipment newEquipment = Equipment.builder()
 *                     .setColor("green")
 *                     .setJedi("luke")
 *                     .setType("lightsaber")
 *                     .build();
 *                 return txn.put(newEquipment);
 *             }
 *         })
 *         .thenCompose(putResult -> {
 *             // Delete another item
 *             txn.delete("/jedi-luke/equipment-old-cloak");
 *             return CompletableFuture.completedFuture(putResult);
 *         });
 * }).get();
 * }</pre>
 */
public interface Transaction {

  /**
   * get retrieves an item by its full key path. This will return the item if it exists, or null if
   * it does not.
   *
   * @param <T> the type of the item to retrieve
   * @param keyPath the full key path of the item
   * @return a CompletableFuture containing the item or null if not found
   *     <p>Example usage:
   *     <pre>{@code
   * CompletableFuture<Equipment> item = txn.get("/jedi-luke/equipment-lightsaber");
   * }</pre>
   */
  <T extends StatelyItem> CompletableFuture<T> get(String keyPath);

  /**
   * getBatch retrieves multiple items by their full key paths. This will return the corresponding
   * items that exist. Use beginList instead if you want to retrieve multiple items but don't
   * already know the full key paths of the items you want to get. You can get items of different
   * types in a single getBatch - you will need to use type checking to determine what item type
   * each item is.
   *
   * @param keyPaths the list of key paths to retrieve
   * @return a CompletableFuture containing the list of items
   *     <p>Example usage:
   *     <pre>{@code
   * List<StatelyItem> items = txn.getBatch(
   *     List.of("/jedi-luke/equipment-lightsaber", "/jedi-luke/equipment-cloak")
   * ).get();
   * for (StatelyItem item : items) {
   *     if (item instanceof Equipment) {
   *         System.out.println("Got an Equipment item: " + item);
   *     }
   * }
   * }</pre>
   */
  CompletableFuture<List<StatelyItem>> getBatch(List<String> keyPaths);

  /**
   * put adds an Item to the Store, or replaces the Item if it already exists at that path. Unlike
   * the put method outside of a transaction, this only returns the generated ID of the item, and
   * then only if the item was newly created and has an `initialValue` field in its key. This is so
   * you can use that ID in subsequent puts to reference newly created items. The final put items
   * will not be returned until the transaction is committed, in which case they will be included in
   * the `TransactionResult.puts` list.
   *
   * @param <T> the type of the item to put
   * @param item the item to put
   * @return a CompletableFuture containing the generated ID if applicable
   *     <p>Example usage:
   *     <pre>{@code
   * Equipment lightsaber = Equipment.builder()
   *     .setColor("green")
   *     .setJedi("luke")
   *     .setType("lightsaber")
   *     .build();
   * GeneratedId id = txn.put(lightsaber).get();
   * }</pre>
   */
  <T extends StatelyItem> CompletableFuture<GeneratedId> put(T item);

  /**
   * put adds an Item to the Store with options, or replaces the Item if it already exists at that
   * path. Unlike the put method outside of a transaction, this only returns the generated ID of the
   * item, and then only if the item was newly created and has an `initialValue` field in its key.
   * This is so you can use that ID in subsequent puts to reference newly created items. The final
   * put items will not be returned until the transaction is committed, in which case they will be
   * included in the `TransactionResult.puts` list.
   *
   * @param <T> the type of the item to put
   * @param request the put request containing the item and options
   * @return a CompletableFuture containing the generated ID if applicable
   */
  <T extends StatelyItem> CompletableFuture<GeneratedId> put(PutRequest<T> request);

  /**
   * putBatch adds multiple Items to the Store, or replaces Items if they already exist at that
   * path. Unlike the putBatch method outside of a transaction, this only returns the generated IDs
   * of the items, and then only if the item was newly created and has an `initialValue` field in
   * its key. The IDs are returned in the same order as the inputs. This is so you can use that ID
   * in subsequent puts to reference newly created items. The final put items will not be returned
   * until the transaction is committed, in which case they will be included in the
   * `TransactionResult.puts` list.
   *
   * @param <T> the type of the items to put
   * @param requests the list of put requests
   * @return a CompletableFuture containing the list of generated IDs
   *     <p>Example usage:
   *     <pre>{@code
   * Equipment lightsaber = Equipment.builder()
   *     .setColor("green")
   *     .setJedi("luke")
   *     .setType("lightsaber")
   *     .build();
   * Equipment cloak = Equipment.builder()
   *     .setColor("brown")
   *     .setJedi("luke")
   *     .setType("cloak")
   *     .build();
   * List<GeneratedId> ids = txn.putBatch(
   *     List.of(PutRequest.builder(lightsaber).build(),
   *             PutRequest.builder(cloak).build())
   * ).get();
   * }</pre>
   */
  <T extends StatelyItem> CompletableFuture<List<GeneratedId>> putBatch(
      List<PutRequest<T>> requests);

  /**
   * delete removes one or more items from the Store by their full key paths. delete succeeds even
   * if there isn't an item at that key path.
   *
   * @param keyPaths the full key paths of the items to delete
   *     <p>Example usage:
   *     <pre>{@code
   * txn.delete("/jedi-luke/equipment-lightsaber", "/jedi-luke/equipment-cloak");
   * }</pre>
   */
  void delete(String... keyPaths);

  /**
   * beginList retrieves Items that start with a specified keyPathPrefix from a single Group.
   * Because it can only list items from a single Group, the key path prefix must at least start
   * with a full Group Key (a single key segment with a namespace and an ID, e.g. `/user-1234`).
   *
   * <p>beginList will return an empty result set if there are no items matching that key prefix.
   * This API returns a token that you can pass to continueList to expand the result set.
   *
   * <p>You can list items of different types in a single beginList, and you can use type checking
   * to handle different item types.
   *
   * @param keyPathPrefix the key path prefix to query for. It must be at least a full Group Key
   *     (e.g. `/user-1234`).
   * @return a CompletableFuture containing a ListResult
   *     <p>Example usage:
   *     <pre>{@code
   * ListResult listResult = txn.beginList("/jedi-luke/equipment").get();
   * for (StatelyItem item : listResult.getItems()) {
   *     if (item instanceof Equipment) {
   *         Equipment equipment = (Equipment) item;
   *         System.out.println(equipment.getColor());
   *     }
   * }
   * ListToken token = listResult.getToken();
   * }</pre>
   */
  CompletableFuture<ListResult> beginList(String keyPathPrefix);

  /**
   * beginList retrieves Items that start with a specified keyPathPrefix from a single Group with
   * options. Because it can only list items from a single Group, the key path prefix must at least
   * start with a full Group Key (a single key segment with a namespace and an ID, e.g.
   * `/user-1234`).
   *
   * <p>beginList will return an empty result set if there are no items matching that key prefix.
   * This API returns a token that you can pass to continueList to expand the result set.
   *
   * <p>You can list items of different types in a single beginList, and you can use type checking
   * to handle different item types.
   *
   * @param keyPathPrefix the key path prefix to query for. It must be at least a full Group Key
   *     (e.g. `/user-1234`).
   * @param options the list options for filtering and pagination
   * @return a CompletableFuture containing a ListResult
   */
  CompletableFuture<ListResult> beginList(String keyPathPrefix, ListOptions options);

  /**
   * continueList takes the token from a beginList call and returns the next "page" of results based
   * on the original query parameters and pagination options. It doesn't have options because it is
   * a continuation of a previous list operation. It will return a new token which can be used for
   * another continueList call, and so on. Calls to continueList are tied to the authorization of
   * the original beginList call, so if the original beginList call was allowed, continueList with
   * its token should also be allowed.
   *
   * <p>You can list items of different types in a single continueList, and you can use type
   * checking to handle different item types.
   *
   * @param token the token from a previous list operation
   * @return a CompletableFuture containing a ListResult
   *     <p>Example usage:
   *     <pre>{@code
   * ListResult continueResult = txn.continueList(token).get();
   * for (StatelyItem item : continueResult.getItems()) {
   *     if (item instanceof Equipment) {
   *         Equipment equipment = (Equipment) item;
   *         System.out.println(equipment.getType());
   *     }
   * }
   * ListToken nextToken = continueResult.getToken();
   * }</pre>
   */
  CompletableFuture<ListResult> continueList(ListToken token);
}
