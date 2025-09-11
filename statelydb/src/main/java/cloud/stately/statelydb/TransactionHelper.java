package cloud.stately.statelydb;

import cloud.stately.db.DatabaseServiceGrpc;
import cloud.stately.db.DeleteItem;
import cloud.stately.db.GetItem;
import cloud.stately.db.ListToken;
import cloud.stately.db.PutItem;
import cloud.stately.db.TransactionBegin;
import cloud.stately.db.TransactionBeginList;
import cloud.stately.db.TransactionContinueList;
import cloud.stately.db.TransactionDelete;
import cloud.stately.db.TransactionGet;
import cloud.stately.db.TransactionPut;
import cloud.stately.db.TransactionRequest;
import cloud.stately.db.TransactionResponse;
import cloud.stately.statelydb.common.StatelyException;
import cloud.stately.statelydb.schema.BaseTypeMapper;
import cloud.stately.statelydb.schema.StatelyItem;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Helper class that implements transaction functionality using a simplified streaming protocol.
 * This class provides the concrete implementation of the Transaction interface and manages the
 * underlying gRPC stream for transaction operations.
 *
 * <p>TransactionHelper coordinates sending requests and awaiting responses for all of the
 * transaction methods. It is passed directly to the user-defined handler function.
 */
public class TransactionHelper implements Transaction, StreamObserver<TransactionResponse> {

  private final BaseTypeMapper typeMapper;
  private final AtomicInteger messageIdCounter = new AtomicInteger(1);
  private StreamObserver<TransactionRequest> requestStream;
  private final ConcurrentLinkedQueue<CompletableFuture<TransactionResponse>> pendingResponses =
      new ConcurrentLinkedQueue<>();
  private AtomicBoolean serverError = new AtomicBoolean(false);

  /**
   * Creates a new TransactionHelper.
   *
   * @param storeId the store ID
   * @param typeMapper the type mapper for marshalling/unmarshalling items
   * @param stub the gRPC stub for database operations
   */
  public TransactionHelper(
      long storeId, BaseTypeMapper typeMapper, DatabaseServiceGrpc.DatabaseServiceStub stub) {

    this.typeMapper = typeMapper;
    this.requestStream = stub.transaction(this);

    // Send begin message
    TransactionRequest beginRequest =
        TransactionRequest.newBuilder()
            .setMessageId(messageIdCounter.getAndIncrement())
            .setBegin(
                TransactionBegin.newBuilder()
                    .setStoreId(storeId)
                    .setSchemaId(typeMapper.getSchemaId())
                    .setSchemaVersionId(typeMapper.getSchemaVersionId())
                    .build())
            .build();

    requestStream.onNext(beginRequest);
  }

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
  @SuppressWarnings("unchecked")
  public <T extends StatelyItem> CompletableFuture<T> get(String keyPath) {
    return getBatch(List.of(keyPath)).thenApply(items -> items.isEmpty() ? null : (T) items.get(0));
  }

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
  public CompletableFuture<List<StatelyItem>> getBatch(List<String> keyPaths) {

    TransactionRequest.Builder requestBuilder =
        TransactionRequest.newBuilder()
            .setGetItems(
                TransactionGet.newBuilder()
                    .addAllGets(
                        keyPaths.stream()
                            .map(keyPath -> GetItem.newBuilder().setKeyPath(keyPath).build())
                            .collect(Collectors.toList()))
                    .build());

    return requestResponse(requestBuilder)
        .thenCompose(
            response -> {
              if (!response.hasGetResults()) {
                CompletableFuture<List<StatelyItem>> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(
                    new StatelyException("Expected get results", Status.Code.INTERNAL, "Internal"));
                return failedFuture;
              }
              return CompletableFuture.completedFuture(
                  response.getGetResults().getItemsList().stream()
                      .map(item -> typeMapper.unmarshal(item))
                      .collect(Collectors.toList()));
            });
  }

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
  public <T extends StatelyItem> CompletableFuture<GeneratedId> put(T item) {
    return put(PutRequest.builder(item).build());
  }

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
  public <T extends StatelyItem> CompletableFuture<GeneratedId> put(PutRequest<T> request) {
    return putBatch(List.of(request)).thenApply(results -> results.get(0));
  }

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
  public <T extends StatelyItem> CompletableFuture<List<GeneratedId>> putBatch(
      List<PutRequest<T>> requests) {

    TransactionRequest.Builder requestBuilder =
        TransactionRequest.newBuilder()
            .setPutItems(
                TransactionPut.newBuilder()
                    .addAllPuts(
                        requests.stream()
                            .map(
                                (PutRequest<T> request) ->
                                    PutItem.newBuilder().setItem(request.item().marshal()).build())
                            .collect(Collectors.toList()))
                    .build());

    return requestResponse(requestBuilder)
        .thenCompose(
            response -> {
              if (!response.hasPutAck()) {
                CompletableFuture<List<GeneratedId>> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(
                    new StatelyException(
                        "Expected put acknowledgment", Status.Code.INTERNAL, "Internal"));
                return failedFuture;
              }
              return CompletableFuture.completedFuture(
                  response.getPutAck().getGeneratedIdsList().stream()
                      .map(GeneratedId::fromProto)
                      .collect(Collectors.toList()));
            });
  }

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
  public void delete(String... keyPaths) {
    TransactionRequest.Builder requestBuilder =
        TransactionRequest.newBuilder()
            .setDeleteItems(
                TransactionDelete.newBuilder()
                    .addAllDeletes(
                        Arrays.stream(keyPaths)
                            .map(keyPath -> DeleteItem.newBuilder().setKeyPath(keyPath).build())
                            .collect(Collectors.toList()))
                    .build());

    requestOnly(requestBuilder);
  }

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
  public CompletableFuture<ListResult> beginList(String keyPathPrefix) {
    return beginList(keyPathPrefix, null);
  }

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
   * @param options the list options for filtering and pagination (can be null)
   * @return a CompletableFuture containing a ListResult
   */
  public CompletableFuture<ListResult> beginList(String keyPathPrefix, ListOptions options) {
    TransactionBeginList.Builder beginListRequestBuilder =
        TransactionBeginList.newBuilder().setKeyPathPrefix(keyPathPrefix);

    if (options != null) {
      beginListRequestBuilder.addAllFilterConditions(options.buildFilterConditions());
      beginListRequestBuilder.addAllKeyConditions(options.buildKeyConditions());
      if (options.getLimit() > 0) {
        beginListRequestBuilder.setLimit(options.getLimit());
      }
      beginListRequestBuilder.setSortDirection(options.getSortDirection());
    }

    TransactionRequest.Builder requestBuilder =
        TransactionRequest.newBuilder().setBeginList(beginListRequestBuilder.build());

    return streamListResponses(requestBuilder);
  }

  /**
   * Initiates streaming list responses for a list operation.
   *
   * @param requestBuilder the transaction request builder for the list operation
   * @return a CompletableFuture containing the complete ListResult
   */
  private CompletableFuture<ListResult> streamListResponses(
      TransactionRequest.Builder requestBuilder) {
    int msgId = requestOnly(requestBuilder);
    return collectListResponses(msgId);
  }

  /**
   * Collects all list responses for a given message ID using iterative approach.
   *
   * @param messageId the message ID for the list operation
   * @return a CompletableFuture containing the complete ListResult
   */
  private CompletableFuture<ListResult> collectListResponses(int messageId) {
    List<StatelyItem> items = new ArrayList<>();

    CompletableFuture<ListResult> collectMore = new CompletableFuture<>();
    collectListResponse(messageId, items, collectMore);
    return collectMore;
  }

  /**
   * Collects a single list response and continues collecting if needed.
   *
   * @param messageId the message ID for the list operation
   * @param items the accumulating list of items
   * @param result the future to complete with the final result
   */
  private void collectListResponse(
      int messageId, List<StatelyItem> items, CompletableFuture<ListResult> result) {

    CompletableFuture<TransactionResponse> responseFuture = new CompletableFuture<>();
    pendingResponses.add(responseFuture);

    waitForResponse(responseFuture, messageId)
        .thenAccept(
            response -> {
              if (!response.hasListResults()) {
                result.completeExceptionally(
                    new StatelyException(
                        "Expected list results", Status.Code.INTERNAL, "Internal"));
                return;
              }

              var listResponse = response.getListResults();
              switch (listResponse.getResponseCase()) {
                case RESULT:
                  items.addAll(
                      listResponse.getResult().getItemsList().stream()
                          .map(typeMapper::unmarshal)
                          .collect(Collectors.toList()));
                  collectListResponse(messageId, items, result);
                  break;

                case FINISHED:
                  result.complete(
                      new ListResult(
                          new ArrayList<>(items), listResponse.getFinished().getToken()));
                  break;

                case RESPONSE_NOT_SET:
                default:
                  result.completeExceptionally(
                      new StatelyException(
                          "Expected RESULT or FINISHED response",
                          Status.Code.INTERNAL,
                          "Internal"));
                  break;
              }
            })
        .exceptionally(
            throwable -> {
              result.completeExceptionally(throwable);
              return null;
            });
  }

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
  public CompletableFuture<ListResult> continueList(ListToken token) {
    TransactionRequest.Builder requestBuilder =
        TransactionRequest.newBuilder()
            .setContinueList(
                TransactionContinueList.newBuilder().setTokenData(token.getTokenData()).build());

    return streamListResponses(requestBuilder);
  }

  /**
   * commit finalizes the transaction, applying all the changes made within it. This is called
   * automatically if the user-provided handler returns without error.
   *
   * @return a CompletableFuture containing the TransactionResult
   */
  public CompletableFuture<TransactionResult> commit() {
    return requestResponse(TransactionRequest.newBuilder().setCommit(Empty.newBuilder().build()))
        .thenApply(
            response -> {
              requestStream.onCompleted();
              return response;
            })
        .thenCompose(
            response -> {
              if (!response.hasFinished()) {
                CompletableFuture<TransactionResult> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(
                    new StatelyException(
                        "Expected commit acknowledgment", Status.Code.INTERNAL, "Internal"));
                return failedFuture;
              }
              return CompletableFuture.completedFuture(
                  new TransactionResult(
                      response.getFinished().getPutResultsList().stream()
                          .map(item -> typeMapper.unmarshal(item))
                          .collect(Collectors.toList()),
                      response.getFinished().getCommitted()));
            });
  }

  /**
   * abort cancels the transaction, discarding all changes made within it. This is called
   * automatically if the handler throws an error.
   *
   * @return a CompletableFuture that completes when the transaction is aborted
   */
  public CompletableFuture<Void> abort() {
    if (serverError.get()) {
      // If there was a server error there is no point to abort.
      // The server will abort the transaction.
      return CompletableFuture.completedFuture(null);
    }
    return requestResponse(TransactionRequest.newBuilder().setAbort(Empty.newBuilder().build()))
        .thenApply(
            response -> {
              requestStream.onCompleted();
              return null;
            });
  }

  /**
   * Waits for a response with the specified message ID and validates it matches.
   *
   * @param responseFuture the future that will contain the response
   * @param messageId the expected message ID
   * @return a CompletableFuture containing the validated TransactionResponse
   */
  private CompletableFuture<TransactionResponse> waitForResponse(
      CompletableFuture<TransactionResponse> responseFuture, int messageId) {
    return responseFuture.thenCompose(
        response -> {
          if (response.getMessageId() != messageId) {
            CompletableFuture<TransactionResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(
                new StatelyException(
                    "Mismatched response message ID",
                    Status.Code.INTERNAL,
                    "Internal: expected " + messageId + " but got " + response.getMessageId()));
            return failedFuture;
          }
          return CompletableFuture.completedFuture(response);
        });
  }

  /**
   * Sends a request and returns a future for the response.
   *
   * @param builder the transaction request builder
   * @return a CompletableFuture containing the TransactionResponse
   */
  private CompletableFuture<TransactionResponse> requestResponse(
      TransactionRequest.Builder builder) {
    int msgId = requestOnly(builder);
    CompletableFuture<TransactionResponse> responseFuture = new CompletableFuture<>();
    pendingResponses.add(responseFuture);
    return waitForResponse(responseFuture, msgId);
  }

  /**
   * Sends a request without expecting a response.
   *
   * @param builder the transaction request builder
   * @return the message ID assigned to the request
   */
  private int requestOnly(TransactionRequest.Builder builder) {
    int msgId = messageIdCounter.getAndIncrement();
    builder.setMessageId(msgId);
    requestStream.onNext(builder.build());
    return msgId;
  }

  // StreamObserver implementation methods
  /** Handles the next response from the transaction stream. */
  @Override
  public void onNext(TransactionResponse response) {
    // Poll should never return null. We implement request
    // response protocol ourselves on the stream.
    pendingResponses.poll().complete(response);
  }

  /**
   * Handles errors from the transaction stream.
   *
   * @param t the throwable that caused the error
   */
  @Override
  public void onError(Throwable t) {
    // set true so abort will be skipped as we already
    // have an error from the server.
    serverError.set(true);

    StatelyException e = StatelyException.from(t);
    for (CompletableFuture<TransactionResponse> future : pendingResponses) {
      future.completeExceptionally(e);
    }
    pendingResponses.clear();
  }

  /**
   * Handles completion of the transaction stream. If there are still pending responses, this
   * indicates an unexpected stream completion and will fail pending futures.
   */
  @Override
  public void onCompleted() {

    // If the stream completes with pending responses, that's an error condition
    if (!pendingResponses.isEmpty()) {
      StatelyException e =
          new StatelyException(
              "Transaction stream completed unexpectedly", Status.Code.INTERNAL, "Internal");
      for (CompletableFuture<TransactionResponse> future : pendingResponses) {
        future.completeExceptionally(e);
      }
      pendingResponses.clear();
    }
  }
}
