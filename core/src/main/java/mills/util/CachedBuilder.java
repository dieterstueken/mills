package mills.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  01.03.2021 19:03
 * modified by: $
 * modified on: $
 */
abstract public class CachedBuilder<V> {

    static final Reference<?> EMPTY = new SoftReference<>(null);

    @SuppressWarnings("unchecked")
    Reference<V> cached = (Reference<V>) EMPTY;

    volatile Supplier<V> builder;

    abstract protected V build();

    protected ForkJoinPool getBuildPool() {
        return ForkJoinPool.commonPool();
    }

    public V get() {
        V value = cached();
        if(value!=null || cached==null)
            return value;

        Supplier<V> builder = this.builder;
        if(builder==null)
            builder = newTask();

        return builder.get();
    }

    public V cached() {
        var cached = this.cached;
        if(cached!=null)
            return cached.get();
        else
            return null;
    }

    private synchronized Supplier<V> newTask() {
        // double check
        Supplier<V> builder = this.builder;
        if(builder!=null)
            return builder;

        // may be the task finished already and left a value
        V value = cached();
        if(value!=null || cached==null)
            return ()->value;

        Task task = new Task();

        // others have to join
        this.builder = task::join;

        // we invoke the task directly
        return task::run;
    }

    /**
     * Set up a built result.
     * If value is null, cached is set to null, too.
     * This indicates, that no further result s are expected.
     * @param value to set up.
     */
    private synchronized void built(V value) {
        if(value==null) {
            // make this finally empty
            cached = null;
            // keep this empty task permanently
        } else {
            // transfer value to a SoftReference
            cached = newReference(value);

            // drop hard reference again
            builder = null;
        }
    }

    private class Task extends RecursiveTask<V>  {

        final Thread worker;

        Task() {
            this.worker = Thread.currentThread();
        }

        V run() {
            return getBuildPool().invoke(this);
        }

        @Override
        protected V compute() {

            V value = build();
            built(value);

            return value;
        }
    }

    protected Reference<V> newReference(V value) {
        return new SoftReference<>(value);
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        if(cached!=null) {
            builder = null;
            cached.enqueue();
        }
    }
}
