package mills.index;

import mills.bits.PopCount;
import mills.ring.RingEntry;

import java.util.Arrays;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.11.12
 * Time: 12:17
 */
abstract class T2Builder {

    final PopCount pop;

    final AtomicInteger todo = new AtomicInteger(RingEntry.MAX_INDEX);

    final R0Table entries[] = new R0Table[RingEntry.MAX_INDEX];

    T2Builder(PopCount pop) {
        this.pop = pop;
    }

    abstract protected T0Builder newBuilder();

    private void fill() {

        T0Builder builder = null;
        ForkJoinTask<Void> helper = null;

        int done = 0;

        int i;
        while((i=todo.decrementAndGet())>=0) {

            final RingEntry e2 = RingEntry.TABLE.get(i);
            final PopCount p2 = pop.sub(e2.pop);

            if (p2 == null)
                entries[i] = R0Table.EMPTY;
            else {
                if (builder == null) // get a local builder
                    builder = newBuilder();
                entries[i] = builder.build(p2, e2);
            }

            if(++done == 9 && i>81)
                helper = new RecursiveAction(){
                    @Override
                    protected void compute() {
                        fill();
                    }
                }.fork();
        }

        if(helper!=null)
            helper.join();
    }

    R2Table build() {

        fill();

        return R2Table.of(pop, Arrays.asList(entries));
    }
}