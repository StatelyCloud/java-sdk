package cloud.stately.db;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * DatabaseService is the service for creating, reading, updating and deleting data
 * in a StatelyDB Store. Creating and modifying Stores is done by
 * stately.dbmanagement.ManagementService.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.73.0)",
    comments = "Source: db/service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DatabaseServiceGrpc {

  private DatabaseServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "stately.db.DatabaseService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.PutRequest,
      cloud.stately.db.PutResponse> getPutMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Put",
      requestType = cloud.stately.db.PutRequest.class,
      responseType = cloud.stately.db.PutResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cloud.stately.db.PutRequest,
      cloud.stately.db.PutResponse> getPutMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.PutRequest, cloud.stately.db.PutResponse> getPutMethod;
    if ((getPutMethod = DatabaseServiceGrpc.getPutMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getPutMethod = DatabaseServiceGrpc.getPutMethod) == null) {
          DatabaseServiceGrpc.getPutMethod = getPutMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.PutRequest, cloud.stately.db.PutResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Put"))
              .setIdempotent(true)
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.PutRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.PutResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("Put"))
              .build();
        }
      }
    }
    return getPutMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.GetRequest,
      cloud.stately.db.GetResponse> getGetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Get",
      requestType = cloud.stately.db.GetRequest.class,
      responseType = cloud.stately.db.GetResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cloud.stately.db.GetRequest,
      cloud.stately.db.GetResponse> getGetMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.GetRequest, cloud.stately.db.GetResponse> getGetMethod;
    if ((getGetMethod = DatabaseServiceGrpc.getGetMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getGetMethod = DatabaseServiceGrpc.getGetMethod) == null) {
          DatabaseServiceGrpc.getGetMethod = getGetMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.GetRequest, cloud.stately.db.GetResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Get"))
              .setSafe(true)
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.GetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.GetResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("Get"))
              .build();
        }
      }
    }
    return getGetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.DeleteRequest,
      cloud.stately.db.DeleteResponse> getDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Delete",
      requestType = cloud.stately.db.DeleteRequest.class,
      responseType = cloud.stately.db.DeleteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cloud.stately.db.DeleteRequest,
      cloud.stately.db.DeleteResponse> getDeleteMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.DeleteRequest, cloud.stately.db.DeleteResponse> getDeleteMethod;
    if ((getDeleteMethod = DatabaseServiceGrpc.getDeleteMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getDeleteMethod = DatabaseServiceGrpc.getDeleteMethod) == null) {
          DatabaseServiceGrpc.getDeleteMethod = getDeleteMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.DeleteRequest, cloud.stately.db.DeleteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Delete"))
              .setIdempotent(true)
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.DeleteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.DeleteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("Delete"))
              .build();
        }
      }
    }
    return getDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.BeginListRequest,
      cloud.stately.db.ListResponse> getBeginListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BeginList",
      requestType = cloud.stately.db.BeginListRequest.class,
      responseType = cloud.stately.db.ListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<cloud.stately.db.BeginListRequest,
      cloud.stately.db.ListResponse> getBeginListMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.BeginListRequest, cloud.stately.db.ListResponse> getBeginListMethod;
    if ((getBeginListMethod = DatabaseServiceGrpc.getBeginListMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getBeginListMethod = DatabaseServiceGrpc.getBeginListMethod) == null) {
          DatabaseServiceGrpc.getBeginListMethod = getBeginListMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.BeginListRequest, cloud.stately.db.ListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BeginList"))
              .setSafe(true)
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.BeginListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.ListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("BeginList"))
              .build();
        }
      }
    }
    return getBeginListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.ContinueListRequest,
      cloud.stately.db.ListResponse> getContinueListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ContinueList",
      requestType = cloud.stately.db.ContinueListRequest.class,
      responseType = cloud.stately.db.ListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<cloud.stately.db.ContinueListRequest,
      cloud.stately.db.ListResponse> getContinueListMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.ContinueListRequest, cloud.stately.db.ListResponse> getContinueListMethod;
    if ((getContinueListMethod = DatabaseServiceGrpc.getContinueListMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getContinueListMethod = DatabaseServiceGrpc.getContinueListMethod) == null) {
          DatabaseServiceGrpc.getContinueListMethod = getContinueListMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.ContinueListRequest, cloud.stately.db.ListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ContinueList"))
              .setSafe(true)
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.ContinueListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.ListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("ContinueList"))
              .build();
        }
      }
    }
    return getContinueListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.BeginScanRequest,
      cloud.stately.db.ListResponse> getBeginScanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BeginScan",
      requestType = cloud.stately.db.BeginScanRequest.class,
      responseType = cloud.stately.db.ListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<cloud.stately.db.BeginScanRequest,
      cloud.stately.db.ListResponse> getBeginScanMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.BeginScanRequest, cloud.stately.db.ListResponse> getBeginScanMethod;
    if ((getBeginScanMethod = DatabaseServiceGrpc.getBeginScanMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getBeginScanMethod = DatabaseServiceGrpc.getBeginScanMethod) == null) {
          DatabaseServiceGrpc.getBeginScanMethod = getBeginScanMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.BeginScanRequest, cloud.stately.db.ListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BeginScan"))
              .setSafe(true)
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.BeginScanRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.ListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("BeginScan"))
              .build();
        }
      }
    }
    return getBeginScanMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.ContinueScanRequest,
      cloud.stately.db.ListResponse> getContinueScanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ContinueScan",
      requestType = cloud.stately.db.ContinueScanRequest.class,
      responseType = cloud.stately.db.ListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<cloud.stately.db.ContinueScanRequest,
      cloud.stately.db.ListResponse> getContinueScanMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.ContinueScanRequest, cloud.stately.db.ListResponse> getContinueScanMethod;
    if ((getContinueScanMethod = DatabaseServiceGrpc.getContinueScanMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getContinueScanMethod = DatabaseServiceGrpc.getContinueScanMethod) == null) {
          DatabaseServiceGrpc.getContinueScanMethod = getContinueScanMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.ContinueScanRequest, cloud.stately.db.ListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ContinueScan"))
              .setSafe(true)
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.ContinueScanRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.ListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("ContinueScan"))
              .build();
        }
      }
    }
    return getContinueScanMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.SyncListRequest,
      cloud.stately.db.SyncListResponse> getSyncListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SyncList",
      requestType = cloud.stately.db.SyncListRequest.class,
      responseType = cloud.stately.db.SyncListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<cloud.stately.db.SyncListRequest,
      cloud.stately.db.SyncListResponse> getSyncListMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.SyncListRequest, cloud.stately.db.SyncListResponse> getSyncListMethod;
    if ((getSyncListMethod = DatabaseServiceGrpc.getSyncListMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getSyncListMethod = DatabaseServiceGrpc.getSyncListMethod) == null) {
          DatabaseServiceGrpc.getSyncListMethod = getSyncListMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.SyncListRequest, cloud.stately.db.SyncListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SyncList"))
              .setSafe(true)
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.SyncListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.SyncListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("SyncList"))
              .build();
        }
      }
    }
    return getSyncListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cloud.stately.db.TransactionRequest,
      cloud.stately.db.TransactionResponse> getTransactionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Transaction",
      requestType = cloud.stately.db.TransactionRequest.class,
      responseType = cloud.stately.db.TransactionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<cloud.stately.db.TransactionRequest,
      cloud.stately.db.TransactionResponse> getTransactionMethod() {
    io.grpc.MethodDescriptor<cloud.stately.db.TransactionRequest, cloud.stately.db.TransactionResponse> getTransactionMethod;
    if ((getTransactionMethod = DatabaseServiceGrpc.getTransactionMethod) == null) {
      synchronized (DatabaseServiceGrpc.class) {
        if ((getTransactionMethod = DatabaseServiceGrpc.getTransactionMethod) == null) {
          DatabaseServiceGrpc.getTransactionMethod = getTransactionMethod =
              io.grpc.MethodDescriptor.<cloud.stately.db.TransactionRequest, cloud.stately.db.TransactionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Transaction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.TransactionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cloud.stately.db.TransactionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DatabaseServiceMethodDescriptorSupplier("Transaction"))
              .build();
        }
      }
    }
    return getTransactionMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DatabaseServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DatabaseServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DatabaseServiceStub>() {
        @java.lang.Override
        public DatabaseServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DatabaseServiceStub(channel, callOptions);
        }
      };
    return DatabaseServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static DatabaseServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DatabaseServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DatabaseServiceBlockingV2Stub>() {
        @java.lang.Override
        public DatabaseServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DatabaseServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return DatabaseServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DatabaseServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DatabaseServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DatabaseServiceBlockingStub>() {
        @java.lang.Override
        public DatabaseServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DatabaseServiceBlockingStub(channel, callOptions);
        }
      };
    return DatabaseServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DatabaseServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DatabaseServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DatabaseServiceFutureStub>() {
        @java.lang.Override
        public DatabaseServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DatabaseServiceFutureStub(channel, callOptions);
        }
      };
    return DatabaseServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * DatabaseService is the service for creating, reading, updating and deleting data
   * in a StatelyDB Store. Creating and modifying Stores is done by
   * stately.dbmanagement.ManagementService.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Put adds one or more Items to the Store, or replaces the Items if they
     * already exist. This will fail if the caller does not have permission to
     * create or update Items, if there is no schema registered for the provided
     * item type, or if an item is invalid. All puts are applied atomically;
     * either all will fail or all will succeed. If an item's schema specifies an
     * `initialValue` for one or more properties used in its key paths, and the
     * item is new, you should not provide those values - the database will choose
     * them for you, and Data must be provided as either serialized binary
     * protobuf or JSON.
     * </pre>
     */
    default void put(cloud.stately.db.PutRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.PutResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get retrieves one or more Items by their key paths. This will return any of
     * the Items that exist. It will fail if the caller does not have permission
     * to read Items. Use the List APIs if you want to retrieve multiple items but
     * don't already know the full key paths of the items you want to get.
     * </pre>
     */
    default void get(cloud.stately.db.GetRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.GetResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetMethod(), responseObserver);
    }

    /**
     * <pre>
     * Delete removes one or more Items from the Store by their key paths. This
     * will fail if the caller does not have permission to delete Items.
     * Tombstones will be saved for deleted items for some time, so
     * that SyncList can return information about deleted items. Deletes are
     * always applied atomically; all will fail or all will succeed.
     * </pre>
     */
    default void delete(cloud.stately.db.DeleteRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.DeleteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteMethod(), responseObserver);
    }

    /**
     * <pre>
     * BeginList retrieves Items that start with a specified key path prefix. The
     * key path prefix must minimally contain a Group Key (a single key segment
     * with a namespace and an ID). BeginList will return an empty result set if
     * there are no items matching that key prefix. This API returns a token that
     * you can pass to ContinueList to expand the result set, or to SyncList to
     * get updates within the result set. This can fail if the caller does not
     * have permission to read Items.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    default void beginList(cloud.stately.db.BeginListRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBeginListMethod(), responseObserver);
    }

    /**
     * <pre>
     * ContinueList takes the token from a BeginList call and returns more results
     * based on the original query parameters and pagination options. It has very
     * few options of its own because it is a continuation of a previous list
     * operation. It will return a new token which can be used for another
     * ContinueList call, and so on. The token is the same one used by SyncList -
     * each time you call either ContinueList or SyncList, you should pass the
     * latest version of the token, and then use the new token from the result in
     * subsequent calls. You may interleave ContinueList and SyncList calls
     * however you like, but it does not make sense to make both calls in
     * parallel. Calls to ContinueList are tied to the authorization of the
     * original BeginList call, so if the original BeginList call was allowed,
     * ContinueList with its token should also be allowed.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    default void continueList(cloud.stately.db.ContinueListRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getContinueListMethod(), responseObserver);
    }

    /**
     * <pre>
     * BeginScan initiates a scan request which will scan over the entire store
     * and apply the provided filters. This API returns a token that you can pass
     * to ContinueScan to paginate through the result set. This can fail if the
     * caller does not have permission to read Items.
     * WARNING: THIS API CAN BE EXTREMELY EXPENSIVE FOR STORES WITH A LARGE NUMBER
     * OF ITEMS.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    default void beginScan(cloud.stately.db.BeginScanRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBeginScanMethod(), responseObserver);
    }

    /**
     * <pre>
     * ContinueScan takes the token from a BeginScan call and returns more results
     * based on the original request parameters and pagination options. It has
     * very few options of its own because it is a continuation of a previous list
     * operation. It will return a new token which can be used for another
     * ContinueScan call, and so on. Calls to ContinueScan are tied to the
     * authorization of the original BeginScan call, so if the original BeginScan
     * call was allowed, ContinueScan with its token should also be allowed.
     * WARNING: THIS API CAN BE EXTREMELY EXPENSIVE FOR STORES WITH A LARGE NUMBER OF ITEMS.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    default void continueScan(cloud.stately.db.ContinueScanRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getContinueScanMethod(), responseObserver);
    }

    /**
     * <pre>
     * SyncList returns all changes to Items within the result set of a previous
     * List operation. For all Items within the result set that were modified, it
     * returns the full Item at in its current state. It also returns a list of
     * Item key paths that were deleted since the last SyncList, which you should
     * reconcile with your view of items returned from previous
     * BeginList/ContinueList calls. Using this API, you can start with an initial
     * set of items from BeginList, and then stay up to date on any changes via
     * repeated SyncList requests over time. The token is the same one used by
     * ContinueList - each time you call either ContinueList or SyncList, you
     * should pass the latest version of the token, and then use the new token
     * from the result in subsequent calls. Note that if the result set has
     * already been expanded to the end (in the direction of the original
     * BeginList request), SyncList will return newly created Items. You may
     * interleave ContinueList and SyncList calls however you like, but it does
     * not make sense to make both calls in parallel. Calls to SyncList are tied
     * to the authorization of the original BeginList call, so if the original
     * BeginList call was allowed, SyncList with its token should also be allowed.
     * </pre>
     */
    default void syncList(cloud.stately.db.SyncListRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.SyncListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSyncListMethod(), responseObserver);
    }

    /**
     * <pre>
     * Transaction performs a transaction, within which you can issue writes
     * (Put/Delete) and reads (Get/List) in any order, followed by a commit
     * message. Reads are guaranteed to reflect the state as of when the
     * transaction started, and writes are committed atomically. This method may
     * fail if another transaction commits before this one finishes - in that
     * case, you should retry your transaction.
     * </pre>
     */
    default io.grpc.stub.StreamObserver<cloud.stately.db.TransactionRequest> transaction(
        io.grpc.stub.StreamObserver<cloud.stately.db.TransactionResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getTransactionMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service DatabaseService.
   * <pre>
   * DatabaseService is the service for creating, reading, updating and deleting data
   * in a StatelyDB Store. Creating and modifying Stores is done by
   * stately.dbmanagement.ManagementService.
   * </pre>
   */
  public static abstract class DatabaseServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return DatabaseServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service DatabaseService.
   * <pre>
   * DatabaseService is the service for creating, reading, updating and deleting data
   * in a StatelyDB Store. Creating and modifying Stores is done by
   * stately.dbmanagement.ManagementService.
   * </pre>
   */
  public static final class DatabaseServiceStub
      extends io.grpc.stub.AbstractAsyncStub<DatabaseServiceStub> {
    private DatabaseServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DatabaseServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DatabaseServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Put adds one or more Items to the Store, or replaces the Items if they
     * already exist. This will fail if the caller does not have permission to
     * create or update Items, if there is no schema registered for the provided
     * item type, or if an item is invalid. All puts are applied atomically;
     * either all will fail or all will succeed. If an item's schema specifies an
     * `initialValue` for one or more properties used in its key paths, and the
     * item is new, you should not provide those values - the database will choose
     * them for you, and Data must be provided as either serialized binary
     * protobuf or JSON.
     * </pre>
     */
    public void put(cloud.stately.db.PutRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.PutResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get retrieves one or more Items by their key paths. This will return any of
     * the Items that exist. It will fail if the caller does not have permission
     * to read Items. Use the List APIs if you want to retrieve multiple items but
     * don't already know the full key paths of the items you want to get.
     * </pre>
     */
    public void get(cloud.stately.db.GetRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.GetResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Delete removes one or more Items from the Store by their key paths. This
     * will fail if the caller does not have permission to delete Items.
     * Tombstones will be saved for deleted items for some time, so
     * that SyncList can return information about deleted items. Deletes are
     * always applied atomically; all will fail or all will succeed.
     * </pre>
     */
    public void delete(cloud.stately.db.DeleteRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.DeleteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * BeginList retrieves Items that start with a specified key path prefix. The
     * key path prefix must minimally contain a Group Key (a single key segment
     * with a namespace and an ID). BeginList will return an empty result set if
     * there are no items matching that key prefix. This API returns a token that
     * you can pass to ContinueList to expand the result set, or to SyncList to
     * get updates within the result set. This can fail if the caller does not
     * have permission to read Items.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    public void beginList(cloud.stately.db.BeginListRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getBeginListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * ContinueList takes the token from a BeginList call and returns more results
     * based on the original query parameters and pagination options. It has very
     * few options of its own because it is a continuation of a previous list
     * operation. It will return a new token which can be used for another
     * ContinueList call, and so on. The token is the same one used by SyncList -
     * each time you call either ContinueList or SyncList, you should pass the
     * latest version of the token, and then use the new token from the result in
     * subsequent calls. You may interleave ContinueList and SyncList calls
     * however you like, but it does not make sense to make both calls in
     * parallel. Calls to ContinueList are tied to the authorization of the
     * original BeginList call, so if the original BeginList call was allowed,
     * ContinueList with its token should also be allowed.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    public void continueList(cloud.stately.db.ContinueListRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getContinueListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * BeginScan initiates a scan request which will scan over the entire store
     * and apply the provided filters. This API returns a token that you can pass
     * to ContinueScan to paginate through the result set. This can fail if the
     * caller does not have permission to read Items.
     * WARNING: THIS API CAN BE EXTREMELY EXPENSIVE FOR STORES WITH A LARGE NUMBER
     * OF ITEMS.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    public void beginScan(cloud.stately.db.BeginScanRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getBeginScanMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * ContinueScan takes the token from a BeginScan call and returns more results
     * based on the original request parameters and pagination options. It has
     * very few options of its own because it is a continuation of a previous list
     * operation. It will return a new token which can be used for another
     * ContinueScan call, and so on. Calls to ContinueScan are tied to the
     * authorization of the original BeginScan call, so if the original BeginScan
     * call was allowed, ContinueScan with its token should also be allowed.
     * WARNING: THIS API CAN BE EXTREMELY EXPENSIVE FOR STORES WITH A LARGE NUMBER OF ITEMS.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    public void continueScan(cloud.stately.db.ContinueScanRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getContinueScanMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * SyncList returns all changes to Items within the result set of a previous
     * List operation. For all Items within the result set that were modified, it
     * returns the full Item at in its current state. It also returns a list of
     * Item key paths that were deleted since the last SyncList, which you should
     * reconcile with your view of items returned from previous
     * BeginList/ContinueList calls. Using this API, you can start with an initial
     * set of items from BeginList, and then stay up to date on any changes via
     * repeated SyncList requests over time. The token is the same one used by
     * ContinueList - each time you call either ContinueList or SyncList, you
     * should pass the latest version of the token, and then use the new token
     * from the result in subsequent calls. Note that if the result set has
     * already been expanded to the end (in the direction of the original
     * BeginList request), SyncList will return newly created Items. You may
     * interleave ContinueList and SyncList calls however you like, but it does
     * not make sense to make both calls in parallel. Calls to SyncList are tied
     * to the authorization of the original BeginList call, so if the original
     * BeginList call was allowed, SyncList with its token should also be allowed.
     * </pre>
     */
    public void syncList(cloud.stately.db.SyncListRequest request,
        io.grpc.stub.StreamObserver<cloud.stately.db.SyncListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getSyncListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Transaction performs a transaction, within which you can issue writes
     * (Put/Delete) and reads (Get/List) in any order, followed by a commit
     * message. Reads are guaranteed to reflect the state as of when the
     * transaction started, and writes are committed atomically. This method may
     * fail if another transaction commits before this one finishes - in that
     * case, you should retry your transaction.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<cloud.stately.db.TransactionRequest> transaction(
        io.grpc.stub.StreamObserver<cloud.stately.db.TransactionResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getTransactionMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service DatabaseService.
   * <pre>
   * DatabaseService is the service for creating, reading, updating and deleting data
   * in a StatelyDB Store. Creating and modifying Stores is done by
   * stately.dbmanagement.ManagementService.
   * </pre>
   */
  public static final class DatabaseServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<DatabaseServiceBlockingV2Stub> {
    private DatabaseServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DatabaseServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DatabaseServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Put adds one or more Items to the Store, or replaces the Items if they
     * already exist. This will fail if the caller does not have permission to
     * create or update Items, if there is no schema registered for the provided
     * item type, or if an item is invalid. All puts are applied atomically;
     * either all will fail or all will succeed. If an item's schema specifies an
     * `initialValue` for one or more properties used in its key paths, and the
     * item is new, you should not provide those values - the database will choose
     * them for you, and Data must be provided as either serialized binary
     * protobuf or JSON.
     * </pre>
     */
    public cloud.stately.db.PutResponse put(cloud.stately.db.PutRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get retrieves one or more Items by their key paths. This will return any of
     * the Items that exist. It will fail if the caller does not have permission
     * to read Items. Use the List APIs if you want to retrieve multiple items but
     * don't already know the full key paths of the items you want to get.
     * </pre>
     */
    public cloud.stately.db.GetResponse get(cloud.stately.db.GetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Delete removes one or more Items from the Store by their key paths. This
     * will fail if the caller does not have permission to delete Items.
     * Tombstones will be saved for deleted items for some time, so
     * that SyncList can return information about deleted items. Deletes are
     * always applied atomically; all will fail or all will succeed.
     * </pre>
     */
    public cloud.stately.db.DeleteResponse delete(cloud.stately.db.DeleteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * BeginList retrieves Items that start with a specified key path prefix. The
     * key path prefix must minimally contain a Group Key (a single key segment
     * with a namespace and an ID). BeginList will return an empty result set if
     * there are no items matching that key prefix. This API returns a token that
     * you can pass to ContinueList to expand the result set, or to SyncList to
     * get updates within the result set. This can fail if the caller does not
     * have permission to read Items.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, cloud.stately.db.ListResponse>
        beginList(cloud.stately.db.BeginListRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getBeginListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ContinueList takes the token from a BeginList call and returns more results
     * based on the original query parameters and pagination options. It has very
     * few options of its own because it is a continuation of a previous list
     * operation. It will return a new token which can be used for another
     * ContinueList call, and so on. The token is the same one used by SyncList -
     * each time you call either ContinueList or SyncList, you should pass the
     * latest version of the token, and then use the new token from the result in
     * subsequent calls. You may interleave ContinueList and SyncList calls
     * however you like, but it does not make sense to make both calls in
     * parallel. Calls to ContinueList are tied to the authorization of the
     * original BeginList call, so if the original BeginList call was allowed,
     * ContinueList with its token should also be allowed.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, cloud.stately.db.ListResponse>
        continueList(cloud.stately.db.ContinueListRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getContinueListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * BeginScan initiates a scan request which will scan over the entire store
     * and apply the provided filters. This API returns a token that you can pass
     * to ContinueScan to paginate through the result set. This can fail if the
     * caller does not have permission to read Items.
     * WARNING: THIS API CAN BE EXTREMELY EXPENSIVE FOR STORES WITH A LARGE NUMBER
     * OF ITEMS.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, cloud.stately.db.ListResponse>
        beginScan(cloud.stately.db.BeginScanRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getBeginScanMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ContinueScan takes the token from a BeginScan call and returns more results
     * based on the original request parameters and pagination options. It has
     * very few options of its own because it is a continuation of a previous list
     * operation. It will return a new token which can be used for another
     * ContinueScan call, and so on. Calls to ContinueScan are tied to the
     * authorization of the original BeginScan call, so if the original BeginScan
     * call was allowed, ContinueScan with its token should also be allowed.
     * WARNING: THIS API CAN BE EXTREMELY EXPENSIVE FOR STORES WITH A LARGE NUMBER OF ITEMS.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, cloud.stately.db.ListResponse>
        continueScan(cloud.stately.db.ContinueScanRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getContinueScanMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * SyncList returns all changes to Items within the result set of a previous
     * List operation. For all Items within the result set that were modified, it
     * returns the full Item at in its current state. It also returns a list of
     * Item key paths that were deleted since the last SyncList, which you should
     * reconcile with your view of items returned from previous
     * BeginList/ContinueList calls. Using this API, you can start with an initial
     * set of items from BeginList, and then stay up to date on any changes via
     * repeated SyncList requests over time. The token is the same one used by
     * ContinueList - each time you call either ContinueList or SyncList, you
     * should pass the latest version of the token, and then use the new token
     * from the result in subsequent calls. Note that if the result set has
     * already been expanded to the end (in the direction of the original
     * BeginList request), SyncList will return newly created Items. You may
     * interleave ContinueList and SyncList calls however you like, but it does
     * not make sense to make both calls in parallel. Calls to SyncList are tied
     * to the authorization of the original BeginList call, so if the original
     * BeginList call was allowed, SyncList with its token should also be allowed.
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, cloud.stately.db.SyncListResponse>
        syncList(cloud.stately.db.SyncListRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getSyncListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Transaction performs a transaction, within which you can issue writes
     * (Put/Delete) and reads (Get/List) in any order, followed by a commit
     * message. Reads are guaranteed to reflect the state as of when the
     * transaction started, and writes are committed atomically. This method may
     * fail if another transaction commits before this one finishes - in that
     * case, you should retry your transaction.
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<cloud.stately.db.TransactionRequest, cloud.stately.db.TransactionResponse>
        transaction() {
      return io.grpc.stub.ClientCalls.blockingBidiStreamingCall(
          getChannel(), getTransactionMethod(), getCallOptions());
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service DatabaseService.
   * <pre>
   * DatabaseService is the service for creating, reading, updating and deleting data
   * in a StatelyDB Store. Creating and modifying Stores is done by
   * stately.dbmanagement.ManagementService.
   * </pre>
   */
  public static final class DatabaseServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<DatabaseServiceBlockingStub> {
    private DatabaseServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DatabaseServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DatabaseServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Put adds one or more Items to the Store, or replaces the Items if they
     * already exist. This will fail if the caller does not have permission to
     * create or update Items, if there is no schema registered for the provided
     * item type, or if an item is invalid. All puts are applied atomically;
     * either all will fail or all will succeed. If an item's schema specifies an
     * `initialValue` for one or more properties used in its key paths, and the
     * item is new, you should not provide those values - the database will choose
     * them for you, and Data must be provided as either serialized binary
     * protobuf or JSON.
     * </pre>
     */
    public cloud.stately.db.PutResponse put(cloud.stately.db.PutRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get retrieves one or more Items by their key paths. This will return any of
     * the Items that exist. It will fail if the caller does not have permission
     * to read Items. Use the List APIs if you want to retrieve multiple items but
     * don't already know the full key paths of the items you want to get.
     * </pre>
     */
    public cloud.stately.db.GetResponse get(cloud.stately.db.GetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Delete removes one or more Items from the Store by their key paths. This
     * will fail if the caller does not have permission to delete Items.
     * Tombstones will be saved for deleted items for some time, so
     * that SyncList can return information about deleted items. Deletes are
     * always applied atomically; all will fail or all will succeed.
     * </pre>
     */
    public cloud.stately.db.DeleteResponse delete(cloud.stately.db.DeleteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * BeginList retrieves Items that start with a specified key path prefix. The
     * key path prefix must minimally contain a Group Key (a single key segment
     * with a namespace and an ID). BeginList will return an empty result set if
     * there are no items matching that key prefix. This API returns a token that
     * you can pass to ContinueList to expand the result set, or to SyncList to
     * get updates within the result set. This can fail if the caller does not
     * have permission to read Items.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    public java.util.Iterator<cloud.stately.db.ListResponse> beginList(
        cloud.stately.db.BeginListRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getBeginListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ContinueList takes the token from a BeginList call and returns more results
     * based on the original query parameters and pagination options. It has very
     * few options of its own because it is a continuation of a previous list
     * operation. It will return a new token which can be used for another
     * ContinueList call, and so on. The token is the same one used by SyncList -
     * each time you call either ContinueList or SyncList, you should pass the
     * latest version of the token, and then use the new token from the result in
     * subsequent calls. You may interleave ContinueList and SyncList calls
     * however you like, but it does not make sense to make both calls in
     * parallel. Calls to ContinueList are tied to the authorization of the
     * original BeginList call, so if the original BeginList call was allowed,
     * ContinueList with its token should also be allowed.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    public java.util.Iterator<cloud.stately.db.ListResponse> continueList(
        cloud.stately.db.ContinueListRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getContinueListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * BeginScan initiates a scan request which will scan over the entire store
     * and apply the provided filters. This API returns a token that you can pass
     * to ContinueScan to paginate through the result set. This can fail if the
     * caller does not have permission to read Items.
     * WARNING: THIS API CAN BE EXTREMELY EXPENSIVE FOR STORES WITH A LARGE NUMBER
     * OF ITEMS.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    public java.util.Iterator<cloud.stately.db.ListResponse> beginScan(
        cloud.stately.db.BeginScanRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getBeginScanMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ContinueScan takes the token from a BeginScan call and returns more results
     * based on the original request parameters and pagination options. It has
     * very few options of its own because it is a continuation of a previous list
     * operation. It will return a new token which can be used for another
     * ContinueScan call, and so on. Calls to ContinueScan are tied to the
     * authorization of the original BeginScan call, so if the original BeginScan
     * call was allowed, ContinueScan with its token should also be allowed.
     * WARNING: THIS API CAN BE EXTREMELY EXPENSIVE FOR STORES WITH A LARGE NUMBER OF ITEMS.
     * buf:lint:ignore RPC_RESPONSE_STANDARD_NAME
     * </pre>
     */
    public java.util.Iterator<cloud.stately.db.ListResponse> continueScan(
        cloud.stately.db.ContinueScanRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getContinueScanMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * SyncList returns all changes to Items within the result set of a previous
     * List operation. For all Items within the result set that were modified, it
     * returns the full Item at in its current state. It also returns a list of
     * Item key paths that were deleted since the last SyncList, which you should
     * reconcile with your view of items returned from previous
     * BeginList/ContinueList calls. Using this API, you can start with an initial
     * set of items from BeginList, and then stay up to date on any changes via
     * repeated SyncList requests over time. The token is the same one used by
     * ContinueList - each time you call either ContinueList or SyncList, you
     * should pass the latest version of the token, and then use the new token
     * from the result in subsequent calls. Note that if the result set has
     * already been expanded to the end (in the direction of the original
     * BeginList request), SyncList will return newly created Items. You may
     * interleave ContinueList and SyncList calls however you like, but it does
     * not make sense to make both calls in parallel. Calls to SyncList are tied
     * to the authorization of the original BeginList call, so if the original
     * BeginList call was allowed, SyncList with its token should also be allowed.
     * </pre>
     */
    public java.util.Iterator<cloud.stately.db.SyncListResponse> syncList(
        cloud.stately.db.SyncListRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getSyncListMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service DatabaseService.
   * <pre>
   * DatabaseService is the service for creating, reading, updating and deleting data
   * in a StatelyDB Store. Creating and modifying Stores is done by
   * stately.dbmanagement.ManagementService.
   * </pre>
   */
  public static final class DatabaseServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<DatabaseServiceFutureStub> {
    private DatabaseServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DatabaseServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DatabaseServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Put adds one or more Items to the Store, or replaces the Items if they
     * already exist. This will fail if the caller does not have permission to
     * create or update Items, if there is no schema registered for the provided
     * item type, or if an item is invalid. All puts are applied atomically;
     * either all will fail or all will succeed. If an item's schema specifies an
     * `initialValue` for one or more properties used in its key paths, and the
     * item is new, you should not provide those values - the database will choose
     * them for you, and Data must be provided as either serialized binary
     * protobuf or JSON.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cloud.stately.db.PutResponse> put(
        cloud.stately.db.PutRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get retrieves one or more Items by their key paths. This will return any of
     * the Items that exist. It will fail if the caller does not have permission
     * to read Items. Use the List APIs if you want to retrieve multiple items but
     * don't already know the full key paths of the items you want to get.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cloud.stately.db.GetResponse> get(
        cloud.stately.db.GetRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Delete removes one or more Items from the Store by their key paths. This
     * will fail if the caller does not have permission to delete Items.
     * Tombstones will be saved for deleted items for some time, so
     * that SyncList can return information about deleted items. Deletes are
     * always applied atomically; all will fail or all will succeed.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cloud.stately.db.DeleteResponse> delete(
        cloud.stately.db.DeleteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PUT = 0;
  private static final int METHODID_GET = 1;
  private static final int METHODID_DELETE = 2;
  private static final int METHODID_BEGIN_LIST = 3;
  private static final int METHODID_CONTINUE_LIST = 4;
  private static final int METHODID_BEGIN_SCAN = 5;
  private static final int METHODID_CONTINUE_SCAN = 6;
  private static final int METHODID_SYNC_LIST = 7;
  private static final int METHODID_TRANSACTION = 8;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PUT:
          serviceImpl.put((cloud.stately.db.PutRequest) request,
              (io.grpc.stub.StreamObserver<cloud.stately.db.PutResponse>) responseObserver);
          break;
        case METHODID_GET:
          serviceImpl.get((cloud.stately.db.GetRequest) request,
              (io.grpc.stub.StreamObserver<cloud.stately.db.GetResponse>) responseObserver);
          break;
        case METHODID_DELETE:
          serviceImpl.delete((cloud.stately.db.DeleteRequest) request,
              (io.grpc.stub.StreamObserver<cloud.stately.db.DeleteResponse>) responseObserver);
          break;
        case METHODID_BEGIN_LIST:
          serviceImpl.beginList((cloud.stately.db.BeginListRequest) request,
              (io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse>) responseObserver);
          break;
        case METHODID_CONTINUE_LIST:
          serviceImpl.continueList((cloud.stately.db.ContinueListRequest) request,
              (io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse>) responseObserver);
          break;
        case METHODID_BEGIN_SCAN:
          serviceImpl.beginScan((cloud.stately.db.BeginScanRequest) request,
              (io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse>) responseObserver);
          break;
        case METHODID_CONTINUE_SCAN:
          serviceImpl.continueScan((cloud.stately.db.ContinueScanRequest) request,
              (io.grpc.stub.StreamObserver<cloud.stately.db.ListResponse>) responseObserver);
          break;
        case METHODID_SYNC_LIST:
          serviceImpl.syncList((cloud.stately.db.SyncListRequest) request,
              (io.grpc.stub.StreamObserver<cloud.stately.db.SyncListResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_TRANSACTION:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.transaction(
              (io.grpc.stub.StreamObserver<cloud.stately.db.TransactionResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getPutMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              cloud.stately.db.PutRequest,
              cloud.stately.db.PutResponse>(
                service, METHODID_PUT)))
        .addMethod(
          getGetMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              cloud.stately.db.GetRequest,
              cloud.stately.db.GetResponse>(
                service, METHODID_GET)))
        .addMethod(
          getDeleteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              cloud.stately.db.DeleteRequest,
              cloud.stately.db.DeleteResponse>(
                service, METHODID_DELETE)))
        .addMethod(
          getBeginListMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              cloud.stately.db.BeginListRequest,
              cloud.stately.db.ListResponse>(
                service, METHODID_BEGIN_LIST)))
        .addMethod(
          getContinueListMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              cloud.stately.db.ContinueListRequest,
              cloud.stately.db.ListResponse>(
                service, METHODID_CONTINUE_LIST)))
        .addMethod(
          getBeginScanMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              cloud.stately.db.BeginScanRequest,
              cloud.stately.db.ListResponse>(
                service, METHODID_BEGIN_SCAN)))
        .addMethod(
          getContinueScanMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              cloud.stately.db.ContinueScanRequest,
              cloud.stately.db.ListResponse>(
                service, METHODID_CONTINUE_SCAN)))
        .addMethod(
          getSyncListMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              cloud.stately.db.SyncListRequest,
              cloud.stately.db.SyncListResponse>(
                service, METHODID_SYNC_LIST)))
        .addMethod(
          getTransactionMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              cloud.stately.db.TransactionRequest,
              cloud.stately.db.TransactionResponse>(
                service, METHODID_TRANSACTION)))
        .build();
  }

  private static abstract class DatabaseServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DatabaseServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return cloud.stately.db.ServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DatabaseService");
    }
  }

  private static final class DatabaseServiceFileDescriptorSupplier
      extends DatabaseServiceBaseDescriptorSupplier {
    DatabaseServiceFileDescriptorSupplier() {}
  }

  private static final class DatabaseServiceMethodDescriptorSupplier
      extends DatabaseServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    DatabaseServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DatabaseServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DatabaseServiceFileDescriptorSupplier())
              .addMethod(getPutMethod())
              .addMethod(getGetMethod())
              .addMethod(getDeleteMethod())
              .addMethod(getBeginListMethod())
              .addMethod(getContinueListMethod())
              .addMethod(getBeginScanMethod())
              .addMethod(getContinueScanMethod())
              .addMethod(getSyncListMethod())
              .addMethod(getTransactionMethod())
              .build();
        }
      }
    }
    return result;
  }
}
