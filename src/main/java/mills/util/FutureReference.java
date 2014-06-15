package mills.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.12.12 11:31
 * modified by: $Author$
 * modified on: $Date$
 */
public class FutureReference<V> implements Supplier<V> {

    private static Reference NONE = new WeakReference<Object>(null);

    // initialized by an empty reference
    private Reference<V> ref = (Reference<V>) NONE;

    // an other task currently running
    final AtomicReference<ForkJoinTask<V>> running = new AtomicReference<>();

    final Supplier<? extends V> factory;

    public FutureReference(Supplier<? extends V> factory) {
        this.factory = factory;
    }

    public V get() {

        // try to get immediately.
        V value = ref.get();
        if (value != null)
            return value;

        // join some already running task or start a new one
        ForkJoinTask<V> task = running.get();

        if (task == null) {
            task = new Computer();
            task.fork();
        }

        value = task.join();

        return value;
    }

    private class Computer extends RecursiveTask<V> {

        // replace any currently running task.
        // if two tasks are started simultaneously the second one hooks to the previously started.
        final ForkJoinTask<V> other = running.getAndSet(this);

        @Override
        protected V compute() {
            try {
                // some other task was already started, join into.
                if (other != null)
                    return other.join();

                // try to get cached value directly
                V value = ref.get();

                if (value == null) {
                    value = factory.get();

                    // setup a new reference
                    ref = new WeakReference<>(value);
                }

                return value;

            } finally {
                // self reset
                running.compareAndSet(this, null);
            }
        }
    }
}
