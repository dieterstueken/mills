package mills.index1;

import mills.bits.PopCount;
import mills.position.Positions;
import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
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

    final R0Table[] entries = new R0Table[RingEntry.MAX_INDEX];

    final AtomicInteger count = new AtomicInteger(0);

    final Supplier<T0Builder> builders;

    final ConcurrentLinkedQueue<T0Builder> queue = new ConcurrentLinkedQueue<>();

    T2Builder(PopCount pop, Supplier<T0Builder> builders) {
        this.pop = pop;
        this.builders = builders;
        Arrays.fill(entries, R0Table.EMPTY);
    }

    T0Builder getBuilder() {
        T0Builder builder = queue.poll();
        if(builder==null)
            builder = builders.get();
        return builder;
    }

    void releaseBuilder(T0Builder builder) {
        queue.offer(builder);
    }

    void compute(RingEntry e2) {
        final PopCount p2 = pop.sub(e2.pop);
        if (p2 != null) {
            T0Builder builder = getBuilder();
            R0Table result = builder.build(p2, e2);
            if(!result.isEmpty()) {
                entries[e2.index] = result;
                releaseBuilder(builder);
                count.incrementAndGet();
            }
        }
    }

    public R2Index build() {

        RingEntry.TABLE.parallelStream().forEach(this::compute);

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