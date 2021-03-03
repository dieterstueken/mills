package mills.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  01.03.2021 19:03
 * modified by: $
 * modified on: $
 */
public class CachedEntry<V> {

    static final Reference<?> EMPTY = new SoftReference<>(null);

    @SuppressWarnings("unchecked")
    Reference<V> cached = (Reference<V>) EMPTY;

    final Supplier<V> builder;
    CompletableFuture<V> task;

    public CachedEntry(Supplier<V> builder) {
        this.builder = builder;
    }

    public V get() {
        Reference<V> cached = this.cached;

        // finally null
        if(cached==null)
            return null;

        V value = cached.get();
        if (value != null)
            return value;

        CompletableFuture<V> task = this.task;
        if (task == null) {
            synchronized (this) {
                task = this.task;
                if (task == null)
                    this.task = task = newTask();
            }
        }

        return task.join();
    }

    public CompletableFuture<V> future() {
        CompletableFuture<V> task = this.task;
        if(task!=null)
            return task;

        // lookup possible cached value
        Reference<V> cached = this.cached;
        if(cached==null) // finally null
            return CompletableFuture.completedFuture(null);

        V value = cached.get();
        if (value != null)
            return CompletableFuture.completedFuture(value);

        // no task nor a cached value
        return task();
    }

    private CompletableFuture<V> task() {
        var task = this.task;
        if (task == null)
            this.task = task = newTask();
        return task;
    }

    private CompletableFuture<V> newTask() {
        task = CompletableFuture.supplyAsync(builder);
        task.thenAccept(this::doCache).thenRunAsync(this::completed);
        return task;
    }

    private void doCache(V value) {
        if(value!=null)
            cached = new SoftReference<>(value);
        else // mark finally null
            cached = null;
    }

    private void completed() {
        task = null;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        if(cached!=null) {
            task = null;
            cached = (Reference<V>) EMPTY;
        }
    }
}
