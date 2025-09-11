package cloud.stately.statelydb;

import cloud.stately.db.ListResponse;
import cloud.stately.db.ListToken;
import cloud.stately.statelydb.common.StatelyException;
import cloud.stately.statelydb.schema.BaseTypeMapper;
import cloud.stately.statelydb.schema.StatelyItem;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * Observer implementation for handling streaming list responses from the StatelyDB API. This class
 * collects items and handles the completion token for list operations.
 */
public class ListObserver implements StreamObserver<ListResponse> {
  private final ConcurrentLinkedDeque<StatelyItem> items = new ConcurrentLinkedDeque<>();
  private ListToken token = null;
  private final BaseTypeMapper typeMapper;
  private final CompletableFuture<ListResult> onComplete;

  /**
   * Creates a new ListObserver.
   *
   * @param typeMapper the type mapper for converting proto items to Java objects
   * @param onComplete the CompletableFuture to complete when the operation finishes
   */
  public ListObserver(BaseTypeMapper typeMapper, CompletableFuture<ListResult> onComplete) {
    this.typeMapper = typeMapper;
    this.onComplete = onComplete;
  }

  @Override
  public void onNext(ListResponse response) {
    switch (response.getResponseCase()) {
      case RESULT:
        items.addAll(
            response.getResult().getItemsList().stream()
                .map(item -> typeMapper.unmarshal(item))
                .collect(Collectors.toList()));
        break;
      case FINISHED:
        token = response.getFinished().getToken();
        break;

      case RESPONSE_NOT_SET:
      default:
        throw new StatelyException(
            "Expected RESULT or FINISHED response", Status.Code.INTERNAL, "Internal");
    }
  }

  @Override
  public void onError(Throwable t) {
    onComplete.completeExceptionally(StatelyException.from(t));
  }

  @Override
  public void onCompleted() {
    onComplete.complete(new ListResult(new ArrayList<>(items), token));
  }
}
