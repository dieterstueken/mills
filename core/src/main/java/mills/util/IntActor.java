package mills.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;

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
public class IntActor implements AutoCloseable {

    ConcurrentLinkedQueue<Integer> mbox = new ConcurrentLinkedQueue<>();

    final AtomicBoolean idle = new AtomicBoolean(true);

    final IntConsumer target;

    public IntActor(IntConsumer target) {
        this.target = target;
    }

    public int size() {
        return mbox.size();
    }

    /**
     * Submit a new Action to be executed concurrently.
     * @param posIndex to be executed.
     * @return # of executed tasks
     */
    public int submit(int posIndex) {
        int processed = 0;
        boolean todo = true;

        do {
            if(idle.compareAndSet(true, false)) {
                try {
                    processed += work();

                    if(todo) {
                        target.accept(posIndex);
                        todo = false;
                        ++processed;
                    }
                } finally {
                    // reset to idle
                    idle.set(true);
                }
            } else if(todo) {
                // someone else is currently working
                mbox.offer(posIndex);
                todo = false;
            }
            // anything remains to do and no one cares about
        } while(isIdle() && !isEmpty());

        return processed;
    }

    /**
     * Execute any pending work.
     * May be called concurrently any time.
     * @return # of executed tasks
     */
    protected int work() {
        int done = 0;

        // execute all posIndex queued
        for (Integer posIndex = mbox.poll(); posIndex != null; posIndex = mbox.poll()) {
            target.accept(posIndex);
            ++done;
        }

        return done;
    }

    public boolean isEmpty() {
        return mbox==null || mbox.isEmpty();
    }

    public boolean isIdle() {
        return idle.get();
    }

    public void close() {
        if(isIdle() && isEmpty())
            return;

        if(idle.compareAndSet(true, false)) {
            try {
                work();
                mbox = null;
            } finally {
                // reset to idle
                idle.set(true);
            }
        } else {
            throw new IllegalStateException("queue still busy");
        }
    }
}