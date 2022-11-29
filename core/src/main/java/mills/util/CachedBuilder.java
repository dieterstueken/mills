package mills.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;

/**
 * A CachedBuilder either has a valid reference to a result, or it starts a task to compute one.
 * Starting a new computing task must be synchronized.
 * If the task becomes ready it caches its result and resets itself.
 * V build() must be implemented.
 * newReference() may be overwritten.
 * Once build() returns null, no further builds are triggered.
 *
 * @param <V>
 */
abstract public class CachedBuilder<V> {

    static final Reference<?> EMPTY = new SoftReference<>(null);

    @SuppressWarnings("unchecked")
    Reference<V> cached = (Reference<V>) EMPTY;

    volatile Supplier<V> builder;

    abstract protected V build();

    public V get() {
        V value = cached();
        if(value!=null || cached==null)
            return value;

        Supplier<V> builder = this.builder;
        if(builder==null)
            builder = newBuilder();

        return builder.get();
    }

    /**
     * Return a possibly cached value or null of not available.
     * @return a possibly cached value.
     */
    public V cached() {
        var cached = this.cached;
        if(cached!=null)
            return cached.get();
        else
            return null;
    }

    /**
     * Set up a new builder if not already done by another thread.
     * @return a supplier for the value.
     */
    private synchronized Supplier<V> newBuilder() {
        // double check
        Supplier<V> builder = this.builder;
        if(builder!=null)
            return builder;

        // may be the task finished already and left a value
        V value = cached();
        if(value!=null || cached==null)
            return ()->value;

        ForkJoinTask<V> task = ForkJoinTask.adapt(this::compute);

        // others must join it
        this.builder = task::join;

        // we invoke the task directly
        return task::invoke;
    }

    /**
     * Compute the value and setup as result.
     * @return the computed value after installing it.
     */
    private V compute() {
        return cache(build());
    }

    /**
     * Set up a built result to cache.
     * If value is null, cached is set to null, too.
     * This indicates, that no further results are expected.
     * @param value to cache.
     */
    private synchronized V cache(V value) {
        try {
            // set up a new cached value or finally set to null.
            cached = value == null ? null : newReference(value);
        } finally {
            // drop any hard reference again
            builder = null;
        }
        return value;
    }

    /**
     * Create a new Reference. May be overwritten.
     * @param value to refer.
     * @return a new Reference.
     */
    protected Reference<V> newReference(V value) {
        return new SoftReference<>(value);
    }

    /**
     * Drop all caches.
     */
    public void clear() {
        Reference<V> cached = this.cached;
        if(cached!=null)
            cached.enqueue();
    }
}
