package mills.util;

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
public class QueueActor<T> {

    final T actor;

    final ConcurrentLinkedQueue<Action<? super T>> mbox = new ConcurrentLinkedQueue<>();

    final AtomicBoolean idle = new AtomicBoolean(true);

    public QueueActor(T actor) {
        this.actor = actor;
    }

    public static <T> QueueActor<T> of(T actor) {
        return new QueueActor<T>(actor);
    }

    public int size() {
        return mbox.size();
    }

    /**
     * Submit a new Action to be executed concurrently.
     * @param action to be executed.
     * @return # of executed tasks
     */
    public int submit(Action<? super T> action) {
        mbox.offer(action);
        return work();
    }

    /**
     * Execute any pending work.
     * May be called concurrently any time.
     * @return # of executed tasks
     */
    private int work() {
        int done = 0;

        // work to do and idle
        while(!mbox.isEmpty() && idle.compareAndSet(true, false)) {

            // execute all actions queued
            for(Action<? super T> action = mbox.poll(); action!=null; action=mbox.poll()) {
                action.act(actor);
                ++done;
            }

            // reset to idle
            idle.set(true);
        }

        return done;
    }

    public boolean idle() {
        return idle.get();
    }

    public void finish() {
        if(idle.get())
            return;

        final RecursiveAction task = new RecursiveAction() {
            @Override
            protected void compute() {}
        };

        submit((actor)->task.complete(null));

        task.join();
    }

    public String toString() {
        return String.format("Queue of %s [%d]", actor, mbox.size());
    }
}
