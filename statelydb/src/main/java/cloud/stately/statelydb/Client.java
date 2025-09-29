package cloud.stately.statelydb;

import cloud.stately.db.BeginListRequest;
import cloud.stately.db.BeginScanRequest;
import cloud.stately.db.ContinueListRequest;
import cloud.stately.db.ContinueScanRequest;
import cloud.stately.db.DatabaseServiceGrpc;
import cloud.stately.db.DeleteItem;
import cloud.stately.db.DeleteRequest;
import cloud.stately.db.GetItem;
import cloud.stately.db.GetRequest;
import cloud.stately.db.ListToken;
import cloud.stately.db.PutItem;
import cloud.stately.db.SyncListRequest;
import cloud.stately.statelydb.auth.AuthTokenCallCredentials;
import cloud.stately.statelydb.auth.AuthTokenProvider;
import cloud.stately.statelydb.auth.TokenProvider;
import cloud.stately.statelydb.common.FutureUtils;
import cloud.stately.statelydb.common.StatelyException;
import cloud.stately.statelydb.schema.BaseTypeMapper;
import cloud.stately.statelydb.schema.StatelyItem;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Client for interacting with the Stately Cloud API. Provides methods for performing CRUD
 * operations on items in a StatelyDB store.
 */
public class Client implements AutoCloseable {

  /** Default endpoint for the Stately Cloud API. */
  private static final String DEFAULT_ENDPOINT_STRING = "https://api.stately.cloud:443";

  /* The StatelyDB store to use for all operations with this client. */
  private final long storeId;

  /* The type mapper to use for converting between proto Items and generated Items. */
  private final BaseTypeMapper typeMapper;

  /* The token provider to use for authentication. */
  private final TokenProvider tokenProvider;

  /* The endpoint to use for API requests. */
  private final URI endpoint;

  /*
   * Whether to disable auth for this client.
   *  This is useful in BYOC mode.
   */
  private final Boolean noAuth;

  /* The scheduler to use for scheduling tasks. */
  private ScheduledExecutorService scheduler;

  /*
   * Whether to allow stale reads.
   * For now this is always false.
   */
  private final Boolean allowStale;

  /** The gRPC channel used to communicate with the StatelyDB API. */
  private final ManagedChannel channel;

  /* The gRPC stub used to communicate with the StatelyDB data API. */
  private final DatabaseServiceGrpc.DatabaseServiceFutureStub futureStub;

  /* The gRPC stub used for streaming data API calls. */
  private final DatabaseServiceGrpc.DatabaseServiceStub observerStub;

  /** Builder for creating Client instances. */
  public static class Builder {
    private long storeId;
    private BaseTypeMapper typeMapper;
    private ScheduledExecutorService scheduler;
    private TokenProvider tokenProvider;
    private URI endpoint;
    private String region;
    private Boolean noAuth = false;

    /**
     * Creates a new builder with the required parameters.
     *
     * @param storeId the store ID
     * @param typeMapper the type mapper for converting between proto and Java types
     * @param scheduler the scheduled executor service
     */
    protected Builder(long storeId, BaseTypeMapper typeMapper, ScheduledExecutorService scheduler) {
      if (typeMapper == null) {
        throw new StatelyException(
            "Type mapper must be set", Status.Code.INVALID_ARGUMENT, "InvalidArgument");
      }
      if (scheduler == null) {
        throw new StatelyException(
            "Scheduler must be set", Status.Code.INVALID_ARGUMENT, "InvalidArgument");
      }
      this.storeId = storeId;
      this.typeMapper = typeMapper;
      this.scheduler = scheduler;
    }

    /**
     * Sets the token provider for authentication.
     *
     * @param tokenProvider the token provider to use
     * @return this builder instance
     */
    public Builder tokenProvider(TokenProvider tokenProvider) {
      this.tokenProvider = tokenProvider;
      return this;
    }

    /**
     * Sets the endpoint URI for the client.
     *
     * @param endpoint the endpoint URI to use
     * @return this builder instance
     */
    public Builder endpoint(URI endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    /**
     * Sets the region for the client.
     *
     * @param region the region to use
     * @return this builder instance
     */
    public Builder region(String region) {
      this.region = region;
      return this;
    }

    /**
     * Disables authentication for the client. This is useful in BYOC mode.
     *
     * @param noAuth whether to disable authentication
     * @return this builder instance
     */
    public Builder noAuth(Boolean noAuth) {
      this.noAuth = noAuth;
      return this;
    }

    /**
     * Builds and returns a new Client instance.
     *
     * @return a new Client instance
     * @throws StatelyException if required parameters are missing
     */
    public Client build() {
      URI resolvedEndpoint = makeEndpoint(endpoint, region);

      ManagedChannelBuilder<?> channelBuilder =
          ManagedChannelBuilder.forAddress(resolvedEndpoint.getHost(), resolvedEndpoint.getPort())
              .executor(scheduler)
              .maxInboundMetadataSize(
                  Integer.MAX_VALUE); // disabled so that large error details don't cause issues
      if (resolvedEndpoint.getScheme().equals("http")) {
        channelBuilder.usePlaintext();
      }
      ManagedChannel channel = channelBuilder.build();

      // create the gRPC stubs.
      DatabaseServiceGrpc.DatabaseServiceFutureStub futureStub =
          DatabaseServiceGrpc.newFutureStub(channel);
      DatabaseServiceGrpc.DatabaseServiceStub observerStub = DatabaseServiceGrpc.newStub(channel);

      TokenProvider resolvedTokenProvider = null;
      if (!noAuth) {
        if (tokenProvider == null) {
          // if no token provider is provided, create a default one
          resolvedTokenProvider = AuthTokenProvider.builder(scheduler).build();
        } else {
          resolvedTokenProvider = tokenProvider;
        }
        resolvedTokenProvider.start(resolvedEndpoint);
        // overwrite the stubs with the authenticated stubs
        CallCredentials callCreds = new AuthTokenCallCredentials(resolvedTokenProvider);
        futureStub = futureStub.withCallCredentials(callCreds);
        observerStub = observerStub.withCallCredentials(callCreds);
      }

      return new Client(
          storeId,
          typeMapper,
          resolvedTokenProvider,
          resolvedEndpoint,
          noAuth,
          false, // allowStale is always false when the client is constructed
          scheduler,
          channel,
          futureStub,
          observerStub);
    }

    private static URI makeEndpoint(URI endpoint, String region) {
      if (endpoint != null) {
        return endpoint;
      }
      if (region == null) {
        try {
          return new URI(DEFAULT_ENDPOINT_STRING);
        } catch (java.net.URISyntaxException e) {
          throw new StatelyException(
              "Invalid default endpoint: " + DEFAULT_ENDPOINT_STRING + ". Please contact support.",
              Status.Code.INTERNAL,
              "Internal");
        }
      }
      if (region.startsWith("aws-")) {
        region = region.substring(4); // Remove "aws-" prefix
      }

      try {
        return new URI("https://" + region + ".api.stately.cloud:443");
      } catch (java.net.URISyntaxException e) {
        throw new StatelyException(
            "Invalid region: " + region, Status.Code.INVALID_ARGUMENT, "InvalidArgument");
      }
    }
  }

  /**
   * Creates a new builder for constructing Client instances.
   *
   * @param storeId the store ID to use
   * @param typeMapper the type mapper to use
   * @param scheduler the scheduled executor service to use
   * @return a new builder instance
   */
  public static Builder builder(
      long storeId, BaseTypeMapper typeMapper, ScheduledExecutorService scheduler) {
    return new Builder(storeId, typeMapper, scheduler);
  }

  /**
   * Creates a new Client instance.
   *
   * @param storeId the store ID
   * @param typeMapper the type mapper for converting between proto and Java types
   * @param tokenProvider the token provider for authentication
   * @param endpoint the API endpoint URI
   * @param noAuth whether to disable authentication
   * @param allowStale whether to allow stale reads
   * @param scheduler the scheduled executor service
   * @param channel the gRPC channel
   * @param futureStub the database service stub
   * @param observerStub the observer service stub
   */
  public Client(
      long storeId,
      BaseTypeMapper typeMapper,
      TokenProvider tokenProvider,
      URI endpoint,
      Boolean noAuth,
      Boolean allowStale,
      ScheduledExecutorService scheduler,
      ManagedChannel channel,
      DatabaseServiceGrpc.DatabaseServiceFutureStub futureStub,
      DatabaseServiceGrpc.DatabaseServiceStub observerStub) {

    // store everything that is required for operation or cloning this client
    this.storeId = storeId;
    this.typeMapper = typeMapper;
    this.tokenProvider = tokenProvider;
    this.endpoint = endpoint;
    this.noAuth = noAuth;
    this.allowStale = allowStale;
    this.scheduler = scheduler;
    this.channel = channel;
    this.futureStub = futureStub;
    this.observerStub = observerStub;
  }

  /**
   * Returns a clone of the client with the allowStale flag set to the provided value.
   *
   * @param allowStale whether to allow stale reads
   * @return a new Client instance with the specified allowStale setting
   */
  public Client allowStale(Boolean allowStale) {
    return new Client(
        this.storeId,
        this.typeMapper,
        this.tokenProvider,
        this.endpoint,
        this.noAuth,
        allowStale,
        this.scheduler,
        this.channel,
        this.futureStub,
        this.observerStub);
  }

  /**
   * get retrieves an item by its full key path. This will return the item if it exists, or null if
   * it does not.
   *
   * @param <T> the type of item to retrieve
   * @param keyPath the full key path of the item
   * @return a CompletableFuture containing the item or null if not found
   *     <p>Example usage:
   *     <pre>{@code
   * Equipment item = client.get("/jedi-luke/equipment-lightsaber").get();
   * }</pre>
   */
  @SuppressWarnings("unchecked")
  public <T extends StatelyItem> CompletableFuture<T> get(String keyPath) {
    CompletableFuture<List<StatelyItem>> itemsFuture = getBatch(List.of(keyPath));
    return itemsFuture.thenApply(
        items -> {
          return items.isEmpty() ? null : (T) items.get(0);
        });
  }

  /**
   * getBatch retrieves multiple items by their full key paths. This will return the corresponding
   * items that exist. Use beginList instead if you want to retrieve multiple items but don't
   * already know the full key paths of the items you want to get. You can get items of different
   * types in a single getBatch - you will need to use `instanceof` to determine what item type each
   * item is.
   *
   * @param keyPaths the full key paths of each item to load
   * @return a CompletableFuture containing the list of items
   *     <p>Example usage:
   *     <pre>{@code
   * List<StatelyItem> items = client.getBatch(
   *     List.of("/jedi-luke/equipment-lightsaber", "/jedi-luke/equipment-cloak")
   * ).get();
   * System.out.println(((Equipment) items.get(0)).getColor());
   * if (items.get(1) instanceof Equipment) {
   *     System.out.println(((Equipment) items.get(1)).getColor());
   * }
   * }</pre>
   */
  public CompletableFuture<List<StatelyItem>> getBatch(List<String> keyPaths) {
    if (keyPaths.isEmpty()) {
      return CompletableFuture.failedFuture(
          new StatelyException(
              "No keyPaths were provided to get", Status.Code.INVALID_ARGUMENT, "InvalidArgument"));
    }
    GetRequest request =
        GetRequest.newBuilder()
            .setStoreId(storeId)
            .setSchemaId(typeMapper.getSchemaId())
            .setSchemaVersionId(typeMapper.getSchemaVersionId())
            .setAllowStale(allowStale)
            .addAllGets(
                keyPaths.stream()
                    .map(keyPath -> GetItem.newBuilder().setKeyPath(keyPath).build())
                    .collect(Collectors.toList()))
            .build();
    return FutureUtils.toCompletable(futureStub.get(request), scheduler)
        .handle(
            (resp, error) -> {
              if (error != null) {
                throw StatelyException.from(error);
              }
              return resp.getItemsList().stream()
                  .map(item -> typeMapper.unmarshal(item))
                  .collect(Collectors.toList());
            });
  }

  /**
   * put adds an Item to the Store, or replaces the Item if it already exists at that path.
   *
   * <p>This call will fail if the Item conflicts with an existing Item at the same path and the
   * mustNotExist option is set, or the item's ID will be chosen with an `initialValue` and one of
   * its other key paths conflicts with an existing item.
   *
   * @param <T> the type of item to put
   * @param request the put request containing the item and options
   * @return a CompletableFuture containing the stored item
   */
  @SuppressWarnings("unchecked")
  public <T extends StatelyItem> CompletableFuture<T> put(PutRequest<T> request) {
    return putBatch(List.of(request)).thenApply(items -> (T) items.get(0));
  }

  /**
   * put adds an Item to the Store, or replaces the Item if it already exists at that path.
   *
   * <p>This call will fail if the Item conflicts with an existing Item at the same path and the
   * mustNotExist option is set, or the item's ID will be chosen with an `initialValue` and one of
   * its other key paths conflicts with an existing item.
   *
   * <p>This is a convenience method that creates a PutRequest for the item and defaults
   * mustNotExist and overwriteMetadataTimestamp to false. If you need to set these options, pass a
   * PutRequest instead.
   *
   * @see #put(PutRequest)
   * @param <T> the type of the items, must extend StatelyItem
   * @param item the item to put
   * @return a CompletableFuture containing the stored item
   *     <p>Example usage:
   *     <pre>{@code
   * Equipment lightsaber = Equipment.builder()
   *     .setColor("green")
   *     .setJedi("luke")
   *     .setType("lightsaber")
   *     .build();
   * Equipment storedItem = client.put(lightsaber).get();
   * // With options:
   * Equipment storedItem2 = client.put(PutRequest.builder(lightsaber)
   *     .mustNotExist(true).build()).get();
   * }</pre>
   */
  public <T extends StatelyItem> CompletableFuture<T> put(T item) {
    return put(PutRequest.builder(item).build());
  }

  /**
   * putBatch adds multiple Items to the Store, or replaces Items if they already exist at that
   * path. You can put items of different types in a single putBatch. All puts in the request are
   * applied atomically - there are no partial successes.
   *
   * <p>This will fail if any Item conflicts with an existing Item at the same path and its
   * MustNotExist option is set, or the item's ID will be chosen with an `initialValue` and one of
   * its other key paths conflicts with an existing item.
   *
   * @param <T> the type of the items, must extend StatelyItem
   * @param requests the list of PutRequest objects to put
   * @return a CompletableFuture containing the list of stored items
   */
  public <T extends StatelyItem> CompletableFuture<List<StatelyItem>> putBatch(
      List<PutRequest<T>> requests) {
    if (requests.isEmpty()) {
      return CompletableFuture.failedFuture(
          new StatelyException(
              "No items were provided to put", Status.Code.INVALID_ARGUMENT, "InvalidArgument"));
    }
    List<PutItem> putItems =
        requests.stream()
            .map(
                request ->
                    PutItem.newBuilder()
                        .setItem(request.item().marshal())
                        .setMustNotExist(request.mustNotExist())
                        .setOverwriteMetadataTimestamps(request.overwriteMetadataTimestamp())
                        .build())
            .collect(Collectors.toList());

    cloud.stately.db.PutRequest request =
        cloud.stately.db.PutRequest.newBuilder()
            .setStoreId(storeId)
            .setSchemaId(typeMapper.getSchemaId())
            .setSchemaVersionId(typeMapper.getSchemaVersionId())
            .addAllPuts(putItems)
            .build();

    return FutureUtils.toCompletable(futureStub.put(request), scheduler)
        .handle(
            (resp, error) -> {
              if (error != null) {
                throw StatelyException.from(error);
              }
              return resp.getItemsList().stream()
                  .map(item -> typeMapper.unmarshal(item))
                  .collect(Collectors.toList());
            });
  }

  /**
   * putBatch adds multiple Items to the Store, or replaces Items if they already exist at that
   * path. You can put items of different types in a single putBatch. All puts in the request are
   * applied atomically - there are no partial successes.
   *
   * <p>This will fail if any Item conflicts with an existing Item at the same path and its
   * MustNotExist option is set, or the item's ID will be chosen with an `initialValue` and one of
   * its other key paths conflicts with an existing item.
   *
   * <p>This is a convenience method that creates PutRequest objects for each item and defaults
   * mustNotExist and overwriteMetadataTimestamp to false.
   *
   * @param <T> the type of the items, must extend StatelyItem
   * @param items the list of items to put
   * @return a CompletableFuture containing the list of stored items
   *     <p>Example usage:
   *     <pre>{@code
   * List<StatelyItem> items = client.putBatch(List.of(
   *     Equipment.builder().setColor("green").setJedi("luke").setType("lightsaber").build(),
   *     Equipment.builder().setColor("brown").setJedi("luke").setType("cloak").build()
   * )).get();
   * }</pre>
   */
  public <T extends StatelyItem> CompletableFuture<List<StatelyItem>> putBatchItems(List<T> items) {
    return putBatch(
        items.stream().map(item -> PutRequest.builder(item).build()).collect(Collectors.toList()));
  }

  /**
   * delete removes one or more items from the Store by their full key paths. delete succeeds even
   * if there isn't an item at that key path. Tombstones will be saved for deleted items for some
   * time, so that syncList can return information about deleted items. Deletes are always applied
   * atomically; all will fail or all will succeed.
   *
   * @param keyPaths the full key paths of the items to delete
   * @return a CompletableFuture that completes when the deletion is done
   *     <p>Example usage:
   *     <pre>{@code
   * client.delete("/jedi-luke/equipment-lightsaber").get();
   * }</pre>
   */
  public CompletableFuture<Void> delete(String... keyPaths) {
    if (keyPaths.length == 0) {
      return CompletableFuture.failedFuture(
          new StatelyException(
              "No keyPaths were provided to delete",
              Status.Code.INVALID_ARGUMENT,
              "InvalidArgument"));
    }
    DeleteRequest request =
        DeleteRequest.newBuilder()
            .setStoreId(storeId)
            .setSchemaId(typeMapper.getSchemaId())
            .setSchemaVersionId(typeMapper.getSchemaVersionId())
            .addAllDeletes(
                Arrays.stream(keyPaths)
                    .map(keyPath -> DeleteItem.newBuilder().setKeyPath(keyPath).build())
                    .collect(Collectors.toList()))
            .build();

    return FutureUtils.toCompletable(futureStub.delete(request), scheduler)
        .handle(
            (resp, error) -> {
              if (error != null) {
                throw StatelyException.from(error);
              }
              return null;
            });
  }

  /**
   * Begins a list operation to retrieve items with the specified key path prefix. This method
   * returns a ListResult that provides streaming access to the results and a token for pagination
   * and sync operations.
   *
   * @param keyPathPrefix the key path prefix to list items for
   * @return a CompletableFuture containing a ListResult with StatelyItems and a ListToken for
   *     continuation or sync
   */
  public CompletableFuture<ListResult> beginList(String keyPathPrefix) {
    return beginList(keyPathPrefix, null);
  }

  /**
   * Begins a list operation to retrieve items with the specified key path prefix and options. This
   * method returns a ListResult that provides streaming access to the results and a token for
   * pagination and sync operations.
   *
   * @param keyPathPrefix the key path prefix to list items for
   * @param options the list options for filtering and pagination (can be null)
   * @return a CompletableFuture containing a ListResult with StatelyItems and a ListToken for
   *     continuation or sync
   */
  public CompletableFuture<ListResult> beginList(String keyPathPrefix, ListOptions options) {
    BeginListRequest.Builder requestBuilder =
        BeginListRequest.newBuilder()
            .setStoreId(storeId)
            .setSchemaId(typeMapper.getSchemaId())
            .setSchemaVersionId(typeMapper.getSchemaVersionId())
            .setKeyPathPrefix(keyPathPrefix)
            .setAllowStale(allowStale);

    if (options != null) {
      requestBuilder.addAllFilterConditions(options.buildFilterConditions());
      requestBuilder.addAllKeyConditions(options.buildKeyConditions());
      if (options.getLimit() > 0) {
        requestBuilder.setLimit(options.getLimit());
      }
      requestBuilder.setSortDirection(options.getSortDirection());
    }

    CompletableFuture<ListResult> onComplete = new CompletableFuture<>();
    observerStub.beginList(requestBuilder.build(), new ListObserver(typeMapper, onComplete));
    return onComplete;
  }

  /**
   * Continues a list operation using a token from a previous beginList or continueList call. This
   * method returns a ListResult that provides streaming access to the additional results and a new
   * token for further pagination and sync operations.
   *
   * @param token the token from a previous list operation
   * @return a CompletableFuture containing a ListResult with StatelyItems and a ListToken for
   *     continuation or sync
   */
  public CompletableFuture<ListResult> continueList(ListToken token) {
    ContinueListRequest request =
        ContinueListRequest.newBuilder()
            .setSchemaId(typeMapper.getSchemaId())
            .setSchemaVersionId(typeMapper.getSchemaVersionId())
            .setTokenData(token.getTokenData())
            .build();

    CompletableFuture<ListResult> onComplete = new CompletableFuture<>();
    observerStub.continueList(request, new ListObserver(typeMapper, onComplete));
    return onComplete;
  }

  /**
   * Syncs a list operation using a token from a previous beginList or continueList call. This
   * method returns a ListResult that provides streaming access to the sync results, containing
   * information about items that have changed, been deleted, or moved outside the list window since
   * the token was created.
   *
   * @param token the token from a previous list operation
   * @return a CompletableFuture containing a SyncResult with updates and a ListToken for
   *     continuation or sync
   */
  public CompletableFuture<SyncResult> syncList(ListToken token) {
    SyncListRequest request =
        SyncListRequest.newBuilder()
            .setSchemaId(typeMapper.getSchemaId())
            .setSchemaVersionId(typeMapper.getSchemaVersionId())
            .setTokenData(token.getTokenData())
            .build();
    CompletableFuture<SyncResult> result = new CompletableFuture<>();
    observerStub.syncList(request, new SyncObserver(typeMapper, result));
    return result;
  }

  /**
   * Begins a scan operation to retrieve items across the entire store with optional filtering. This
   * method returns a ListResult that provides access to the results and a token for pagination.
   *
   * <p>WARNING: This API can be expensive for stores with a large number of items.
   *
   * @return a CompletableFuture containing a ListResult with StatelyItems and a ListToken for
   *     continuation
   */
  public CompletableFuture<ListResult> beginScan() {
    return beginScan(null);
  }

  /**
   * Begins a scan operation to retrieve items across the entire store with optional filtering and
   * configuration. This method returns a ListResult that provides access to the results and a token
   * for pagination.
   *
   * <p>WARNING: This API can be expensive for stores with a large number of items.
   *
   * @param options the scan options for filtering and configuration (can be null)
   * @return a CompletableFuture containing a ListResult with StatelyItems and a ListToken for
   *     continuation
   */
  public CompletableFuture<ListResult> beginScan(ScanOptions options) {
    BeginScanRequest.Builder requestBuilder =
        BeginScanRequest.newBuilder()
            .setStoreId(storeId)
            .setSchemaId(typeMapper.getSchemaId())
            .setSchemaVersionId(typeMapper.getSchemaVersionId());

    if (options != null) {
      requestBuilder.addAllFilterConditions(options.buildFilterConditions());
      if (options.getLimit() > 0) {
        requestBuilder.setLimit(options.getLimit());
      }
      if (options.buildSegmentationParams() != null) {
        requestBuilder.setSegmentationParams(options.buildSegmentationParams());
      }
    }

    CompletableFuture<ListResult> onComplete = new CompletableFuture<>();
    observerStub.beginScan(requestBuilder.build(), new ListObserver(typeMapper, onComplete));
    return onComplete;
  }

  /**
   * Continues a scan operation using a token from a previous beginScan or continueScan call. This
   * method returns a ListResult that provides access to additional results and a new token for
   * further pagination.
   *
   * @param token the token from a previous scan operation
   * @return a CompletableFuture containing a ListResult with StatelyItem objects and a ListToken
   *     for continuation
   */
  public CompletableFuture<ListResult> continueScan(ListToken token) {
    ContinueScanRequest request =
        ContinueScanRequest.newBuilder()
            .setSchemaId(typeMapper.getSchemaId())
            .setSchemaVersionId(typeMapper.getSchemaVersionId())
            .setTokenData(token.getTokenData())
            .build();

    CompletableFuture<ListResult> onComplete = new CompletableFuture<>();
    observerStub.continueScan(request, new ListObserver(typeMapper, onComplete));
    return onComplete;
  }

  /**
   * transaction allows you to issue reads and writes in any order, and all writes will either
   * succeed or all will fail when the transaction finishes.
   *
   * <p>Reads are guaranteed to reflect the state as of when the transaction started. A transaction
   * may fail if another transaction commits before this one finishes - in that case, you should
   * retry your transaction.
   *
   * <p>If any error occurs during the transaction handler execution, the transaction is aborted and
   * none of the changes made in it will be applied. If the handler returns without error, the
   * transaction is automatically committed.
   *
   * <p>When the transaction is committed, the result will contain the full version of any items
   * that were put in the transaction, and the committed property will be true. If the transaction
   * was aborted, the committed property will be false.
   *
   * @param handler the transaction handler function
   * @return a CompletableFuture containing the transaction result
   * @throws StatelyException if the transaction fails
   *     <p>Example usage:
   *     <pre>{@code
   * TransactionResult result = client.transaction(txn -> {
   *     return txn.get("/jedi-luke/equipment-lightsaber")
   *         .thenCompose(item -> {
   *             if (item != null && item instanceof Equipment) {
   *                 Equipment equipment = (Equipment) item;
   *                 if ("red".equals(equipment.getColor())) {
   *                     equipment.setColor("green");
   *                     return txn.put(equipment).thenApply(id -> null);
   *                 }
   *             }
   *             return CompletableFuture.completedFuture(null);
   *         });
   * }).get();
   *
   * assert result.isCommitted();
   * assert result.getPuts().size() == 1;
   * }</pre>
   */
  public CompletableFuture<TransactionResult> transaction(TransactionHandler handler) {
    TransactionHelper txn = new TransactionHelper(storeId, typeMapper, observerStub);
    try {
      return handler
          .run(txn)
          // if there were no errors in run() and it returned a success then
          // try and commit the transaction. otherwise abort and propagate the error.
          .handle(
              (result, throwable) -> {
                if (throwable != null) {
                  return txn.abort()
                      .thenCompose(
                          v ->
                              CompletableFuture.<TransactionResult>failedFuture(
                                  StatelyException.from(throwable)));
                } else {
                  return txn.commit();
                }
              })
          .thenCompose(future -> future);
    } catch (Throwable t) {
      return txn.abort().thenCompose(v -> CompletableFuture.failedFuture(StatelyException.from(t)));
    }
  }

  @Override
  public void close() throws Exception {
    channel.shutdownNow();
    tokenProvider.close();
  }

  /** Functional interface for transaction handlers. */
  @FunctionalInterface
  public interface TransactionHandler {
    /**
     * Runs the transaction logic.
     *
     * @param transaction the transaction context
     * @return a CompletableFuture that completes when the handler is done
     * @throws Throwable if the transaction handler encounters an error
     */
    CompletableFuture<Void> run(Transaction transaction) throws Throwable;
  }
}
