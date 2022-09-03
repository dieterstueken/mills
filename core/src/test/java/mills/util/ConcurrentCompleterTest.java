package mills.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.09.22
 * Time: 13:25
 */
class ConcurrentCompleterTest {

    static class DebugCompleter extends ConcurrentCompleter {

        int completed = 0;

        protected DebugCompleter(final CountedCompleter<?> parent, final AtomicInteger todo) {
            super(parent, todo);
        }

        protected void compute(int i) {
            double[] random = ThreadLocalRandom.current().doubles(32*1024).toArray();
            Arrays.sort(random);
            System.out.println(i);
            ++completed;
        }

        protected DebugCompleter newSubtask() {
            DebugCompleter completer = new DebugCompleter(this, todo);
            System.out.println("new subtask: " + completer.toString());
            return completer;
        }

        @Override
        public void onCompletion(final CountedCompleter<?> caller) {
            super.onCompletion(caller);
            System.out.println("completed: " + this + " with " + completed + " completions");
        }
    }

    @Test
    void complete() {
        ConcurrentCompleter completer = new DebugCompleter(null, new AtomicInteger(128));
        completer.invoke();
    }
}