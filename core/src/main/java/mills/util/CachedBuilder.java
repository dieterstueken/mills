package mills.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

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

    volatile ForkJoinTask<V> task;

    abstract protected V build();

    public V get() {
        Reference<V> cached = this.cached;

        // finally null
        if(cached==null)
            return null;

        V value = cached.get();
        if(value!=null)
            return value;

        ForkJoinTask<V> task = this.task;
        if(task==null)
            task = newTask();

        return task.join();
    }

    public V cached() {
        var cached = this.cached;
        if(cached!=null)
            return cached.get();
        else return null;
    }

    private synchronized ForkJoinTask<V> newTask() {
        // double check
        ForkJoinTask<V> task = this.task;
        if(task==null)
            task = new Task();

        return task;
    }

    private class Task extends RecursiveTask<V> {

        Task() {
            // race conditions:
           if(cached==null) {
               // finally empty
               complete(null);
           } else {
               V value = cached.get();
               if (value != null) {
                   // already done, don't save
                   complete(value);
               } else {
                   // fork and setup as pending task
                   fork();
                   task = this;
               }
           }
        }

        @Override
        protected V compute() {
            V value = cached();

            if(value==null && cached!=null)
                value = build();

            if(value==null) {
                // make finally empty
                cached = null;
                // keep this empty task permanently
            } else {
                // transfer value to a SoftReference
                cached = new SoftReference<>(value);

                // drop hard reference again
                task = null;
            }

            return value;
        }
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        if(cached!=null) {
            task = null;
            cached = (Reference<V>) EMPTY;
        }
    }
}
