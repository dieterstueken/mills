package mills.index;

import mills.bits.PopCount;
import mills.position.Positions;
import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.11.12
 * Time: 12:17
 */
class T2Builder {

    final PopCount pop;

    final AtomicInteger todo = new AtomicInteger(RingEntry.MAX_INDEX);

    final R0Table entries[] = new R0Table[RingEntry.MAX_INDEX];

    final AtomicInteger count = new AtomicInteger(0);

    final Supplier<T0Builder> builders;

    T2Builder(PopCount pop, Supplier<T0Builder> builders) {
        this.pop = pop;
        this.builders = builders;
    }

    private void fillRemaining() {

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
                    builder = builders.get();
                entries[i] = builder.build(p2, e2);
                count.incrementAndGet();
            }

            if(++done == 9 && i>81)
                helper = new RecursiveAction(){
                    @Override
                    protected void compute() {
                        fillRemaining();
                    }
                }.fork();
        }

        if(helper!=null)
            helper.join();
    }

    public R2Index build() {

        fillRemaining();

        // return empty list
        if(count.get()==0)
            return new R2Index(pop, List.of());

        int index = 0;
        final List<R2Entry> sparseTable = new ArrayList<>(count.get());

        // create a full table if 30% of all entries are occupied
        final List<R2Entry> fullTable = 3*count.get()>RingEntry.MAX_INDEX ? new ArrayList<>(RingEntry.MAX_INDEX) : null;

        for (short i2 = 0; i2 < RingEntry.MAX_INDEX; i2++) {

            R0Table r0t = entries[i2];
            int count = r0t.range();
            index += count;

            if(count!=0 || fullTable!=null) {
                final R2Entry entry = new R2Entry(index, i2, r0t);
                if(count>0)
                    sparseTable.add(entry);
                if(fullTable!=null)
                    fullTable.add(entry);
            }

        }

        if(fullTable==null)
            return new R2Index(pop, sparseTable);
        else
            return new R2Index(pop, sparseTable) {
                public int posIndex(long i201) {
                    assert verify(i201) : Positions.position(i201);

                    final long n201 = Positions.normalize(i201);
                    final short i2 = Positions.i2(n201);
                    final R2Entry entry = fullTable.get(i2);
                    return entry.posIndex(n201);
                }
            };
    }
}