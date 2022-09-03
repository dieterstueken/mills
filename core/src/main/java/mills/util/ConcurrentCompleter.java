package mills.util;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.09.22
 * Time: 12:58
 */
abstract public class ConcurrentCompleter extends CountedCompleter<Void> {

    public static ConcurrentCompleter compute(int count, IntConsumer action) {
        ConcurrentCompleter completer = completer(null, new AtomicInteger(count), action);
        completer.invoke();
        return completer;
    }

    public static ConcurrentCompleter completer(CountedCompleter<?> parent, AtomicInteger todo, IntConsumer action) {
        return new ConcurrentCompleter(parent, todo) {

            @Override
            protected void compute(final int i) {
                action.accept(i);
            }

            @Override
            protected ConcurrentCompleter newSubtask() {
                return completer(this, todo, action);
            }
        };
    }

    protected final AtomicInteger todo;

    protected ConcurrentCompleter(CountedCompleter<?> parent, AtomicInteger todo) {
        super(parent);
        this.todo = todo;
    }

    int todo() {
        return todo.get();
    }

    int next() {
        return todo.decrementAndGet();
    }

    abstract protected void compute(int i);

    public void compute() {
        // possible fork
        boolean forked = false;

        for(int i = next(); i>=0; i = next()) {

            if(!forked) {
                if(todo()>5) {
                    setPendingCount(1);
                    newSubtask().fork();
                }
                forked = true;
            }

            compute(i);
        }

        tryComplete();
    }

    abstract protected ConcurrentCompleter newSubtask();

    public String toString() {
        return getClass().getSimpleName() + "{" + depth() + "}";
    }

    int depth() {
        int n=0;
        CountedCompleter<?> c = getCompleter();
        while(c!=null) {
            ++n;
            c = c.getCompleter();
        }
        return n;
    }
}
