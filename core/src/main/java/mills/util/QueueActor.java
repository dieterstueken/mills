package mills.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class QueueActor implements AutoCloseable {

    final ConcurrentLinkedQueue<Runnable> mbox = new ConcurrentLinkedQueue<>();

    final AtomicBoolean idle = new AtomicBoolean(true);

    public int size() {
        return mbox.size();
    }

    /**
     * Submit a new Action to be executed concurrently.
     * @param action to be executed.
     * @return # of executed tasks
     */
    public int submit(Runnable action) {
        int processed = 0;

        do {
            if(idle.compareAndSet(true, false)) {
                try {
                    processed += work();
                    // process any local action
                    if(action!=null) {
                        action.run();
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

        // execute all actions queued
        for (Runnable action = mbox.poll(); action != null; action = mbox.poll()) {
            action.run();
            ++done;
        }

        return done;
    }

    public boolean isIdle() {
        return idle.get();
    }

    public void close() {
        if(isIdle())
            return;

        final RecursiveAction task = new RecursiveAction() {
            @Override
            protected void compute() {}
        };

        submit(()->task.complete(null));

        task.join();
    }

    static final Reference<Object> EMPTY = new SoftReference<>(null);

    @SuppressWarnings("unchecked")
    static <T> Reference<T> emptyRef() {
        return (Reference<T>)EMPTY;
    }
}