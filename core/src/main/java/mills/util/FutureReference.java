package mills.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.12.12 11:31
 * modified by: $Author$
 * modified on: $Date$
 */
public class FutureReference<V> {

    // factory to be called on creation
    private final Supplier<? extends V> factory;

    private Computer computer = new Computer();

    private FutureReference(Supplier<? extends V> factory) {
        this.factory = factory;
    }

    public static <V> FutureReference<V> of(Supplier<? extends V> factory) {
        return new FutureReference<V>(factory);
    }

    public static <V> List<FutureReference<V>> of(List<? extends V> source) {
        return AbstractRandomList.generate(source.size(), i->of(()->source.get(i)));
    }

    public static <V> List<V> wrap(List<? extends V> source) {
        return AbstractRandomList.transform(FutureReference.of(source), FutureReference::get);
    }

    public V get() {
        return computer.getValue();
    }

    public void clear() {
        computer.ref.clear();
    }

    private static final Reference EMPTY = new WeakReference<>(null);

    /**
     * Class Computer either has a cached value or it can compute a new one atomically.
     * A Computer is inherently threadsafe and may be called concurrently any time.
     * After a value was computed (due to an empty reference) this computer holds a hard reference to it.
     * To enable GC of the computed value the computer replaces itself by a new computer holding a weak reference only.
     */
    private class Computer extends RecursiveTask<V> {

        final Reference<V> ref;
        final AtomicBoolean running = new AtomicBoolean(false);

        Computer(Reference<V> ref) {
            this.ref = ref;
        }

        @SuppressWarnings("unchecked")
        Computer() {
            this((Reference<V>) EMPTY);
        }

        V getValue() {
            // try to get immediately
            V value = ref.get();
            if (value != null)
                return value;

            // start computation once.
            if(!running.getAndSet(true))
                fork();

            // wait for computation result.
            return join();
        }

        protected V compute() {
            V value = factory.get();
            Reference<V> ref = new WeakReference<>(value);
            // setup new computer
            // and unlink itself to enable CG of value.
            computer = new Computer(ref);
            return value;
        }
    }
}
