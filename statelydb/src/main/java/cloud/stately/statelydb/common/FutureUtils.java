package cloud.stately.statelydb.common;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Utility methods for working with futures and asynchronous operations. */
public class FutureUtils {
  private FutureUtils() {} // prevent instantiation

  /**
   * Convert a Guava ListenableFuture to a Java CompletableFuture.
   *
   * @param <T> The type of the future result
   * @param lf The ListenableFuture to convert
   * @param executor The executor to use for the callback
   * @return A CompletableFuture equivalent
   */
  public static <T> CompletableFuture<T> toCompletable(ListenableFuture<T> lf, Executor executor) {
    CompletableFuture<T> cf = new CompletableFuture<>();
    Futures.addCallback(
        lf,
        new FutureCallback<T>() {
          @Override
          public void onSuccess(@javax.annotation.Nonnull T result) {
            cf.complete(result);
          }

          @Override
          public void onFailure(@javax.annotation.Nonnull Throwable t) {
            cf.completeExceptionally(t);
          }
        },
        MoreExecutors.directExecutor());
    return cf;
  }

  /**
   * Create a CompletableFuture that completes after the specified timeout. This is useful when you
   * want to implement a non-blocking sleep or delay.
   *
   * @param millis The timeout in milliseconds
   * @param scheduler The scheduler to use for the timeout
   * @return A CompletableFuture that completes after the timeout
   */
  public static CompletableFuture<Void> setTimeout(
      long millis, ScheduledExecutorService scheduler) {
    CompletableFuture<Void> promise = new CompletableFuture<>();
    scheduler.schedule(() -> promise.complete(null), millis, TimeUnit.MILLISECONDS);
    return promise;
  }
}
