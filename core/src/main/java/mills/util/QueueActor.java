package mills.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  14.05.13 11:40
 * modified by: $Author$
 * modified on: $Date$
 */

/**
 * Class QueueActor executes submitted actions sequentially like synchronized operations.
 * If the Actor is idle, the action is executed immediately.
 * If the actor is busy the action is queued to be executed by someone else.
 */
abstract public class QueueActor<T> implements AutoCloseable {

    abstract protected T actor();

    final ConcurrentLinkedQueue<Consumer<? super T>> mbox = new ConcurrentLinkedQueue<>();

    final AtomicBoolean idle = new AtomicBoolean(true);

    public int size() {
        return mbox.size();
    }

    /**
     * Submit a new Action to be executed concurrently.
     * @param action to be executed.
     * @return # of executed tasks
     */
    public int submit(Consumer<? super T> action) {
        int processed = 0;

        do {
            if(idle.compareAndSet(true, false)) {
                try {
                    processed += work();
                    // process any local action
                    if(action!=null) {
                        action.accept(actor());
                        action = null;
                        ++processed;
                    }
                } finally {
                    // reset to idle
                    idle.set(true);
                }
            } else if(action!=null) {
                // someone else is currently working
                mbox.offer(action);
                action = null;
            }
            // anything to do
        } while(!(mbox.isEmpty()));

        return processed;
    }

    /**
     * Execute any pending work.
     * May be called concurrently any time.
     * @return # of executed tasks
     */
    protected int work() {
        int done = 0;

        T actor = actor();

        // execute all actions queued
        for (Consumer<? super T> action = mbox.poll(); action != null; action = mbox.poll()) {
            action.accept(actor);
            ++done;
        }

        return done;
    }

    public boolean isIdle() {
        return idle.get();
    }

    public void close() {
        if(idle.get())
            return;

        final RecursiveAction task = new RecursiveAction() {
            @Override
            protected void compute() {}
        };

        submit((actor)->task.complete(null));

        task.join();
    }

    public static <T> QueueActor<T> of(T actor) {
        return new QueueActor<T>() {
            @Override
            protected T actor() {
                return actor;
            }

            public String toString() {
                return String.format("Queue of %s [%d]", actor, mbox.size());
            }
        };
    }

    static final Reference<Object> EMPTY = new SoftReference<>(null);

    @SuppressWarnings("unchecked")
    static <T> Reference<T> emptyRef() {
        return (Reference<T>)EMPTY;
    }

    public static <T> QueueActor<T> lazy(Supplier<T> factory) {

        return new QueueActor<T>() {

            Reference<T> ref = emptyRef();

            @Override
            protected T actor() {
                T actor = ref.get();
                if(actor==null) {
                    actor = factory.get();
                    ref = new SoftReference<>(actor);
                }
                return actor;
            }
        };
    }
}