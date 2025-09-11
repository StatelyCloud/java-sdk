package cloud.stately.statelydb;

import cloud.stately.db.ListToken;
import cloud.stately.db.SyncListResponse;
import cloud.stately.statelydb.common.StatelyException;
import cloud.stately.statelydb.schema.BaseTypeMapper;
import cloud.stately.statelydb.schema.StatelyItem;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Observer implementation for handling streaming sync list responses from the StatelyDB API. This
 * class collects changed items, deleted item paths, and items updated outside the list window.
 */
public class SyncObserver implements StreamObserver<SyncListResponse> {

  private final BaseTypeMapper typeMapper;
  private final CompletableFuture<SyncResult> onComplete;
  private final List<StatelyItem> changedItems = new ArrayList<>();
  private final List<String> deletedItemPaths = new ArrayList<>();
  private final List<String> updatedOutsideListWindowPaths = new ArrayList<>();
  private ListToken token = null;
  private boolean isReset = false;
  private final Lock resultLock = new ReentrantLock();

  /**
   * Creates a new SyncObserver.
   *
   * @param typeMapper the type mapper for converting proto items to Java objects
   * @param onComplete the CompletableFuture to complete when the sync operation finishes
   */
  public SyncObserver(BaseTypeMapper typeMapper, CompletableFuture<SyncResult> onComplete) {
    this.typeMapper = typeMapper;
    this.onComplete = onComplete;
  }

  @Override
  public void onNext(SyncListResponse response) {
    resultLock.lock();
    try {
      switch (response.getResponseCase()) {
        case RESET:
          // override the result and empty everything before the reset
          isReset = true;
          changedItems.clear();
          deletedItemPaths.clear();
          updatedOutsideListWindowPaths.clear();
          break;
        case RESULT:
          changedItems.addAll(
              response.getResult().getChangedItemsList().stream()
                  .map(item -> typeMapper.unmarshal(item))
                  .collect(Collectors.toList()));
          deletedItemPaths.addAll(
              response.getResult().getDeletedItemsList().stream()
                  .map(item -> item.getKeyPath())
                  .collect(Collectors.toList()));
          updatedOutsideListWindowPaths.addAll(
              response.getResult().getUpdatedItemKeysOutsideListWindowList().stream()
                  .collect(Collectors.toList()));
          break;
        case FINISHED:
          token = response.getFinished().getToken();
          break;
        case RESPONSE_NOT_SET:
        default:
          throw new StatelyException(
              "Expected RESET, RESULT or FINISHED response", Status.Code.INTERNAL, "Internal");
      }
    } finally {
      resultLock.unlock();
    }
  }

  @Override
  public void onError(Throwable t) {
    onComplete.completeExceptionally(StatelyException.from(t));
  }

  @Override
  public void onCompleted() {
    resultLock.lock();
    try {
      onComplete.complete(
          new SyncResult(
              changedItems, deletedItemPaths, updatedOutsideListWindowPaths, isReset, token));
    } finally {
      resultLock.unlock();
    }
  }
}
