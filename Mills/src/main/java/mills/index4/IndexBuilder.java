package mills.index4;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.IndexedMap;
import mills.ring.RingEntry;
import mills.util.AbstractRandomArray;
import mills.util.IndexTable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/22/15
 * Time: 11:49 AM
 */
public class IndexBuilder {

    final Partitions partitions;

    public IndexBuilder(Partitions partitions) {
        this.partitions = partitions;
    }

    class T0Builder extends RecursiveTask<IndexedMap<EntryTable>> {

        T0Builder(Object dummy) {}

        final IntStream.Builder keyset = IntStream.builder();

        RingEntry entry(int key) {
            return RingEntry.TABLE.get(key%RingEntry.MAX_INDEX);
        }

        @Override
        protected IndexedMap<EntryTable> compute() {
            int keys[] = keyset.build().toArray();
            EntryTable t0 = partitions.registry.table(AbstractRandomArray.virtual(keys.length, i -> entry(keys[i])));

            short s1[] = new short[keys.length];
            for (int i = 0; i < keys.length; ++i)
                s1[i] = (short) (keys[i] / RingEntry.MAX_INDEX);

            List<EntryTable> t1 = AbstractRandomArray.virtual(keys.length, i -> partitions.registry.get(s1[i]));
            IndexTable it = IndexTable.sum(t1, EntryTable::size);
            return new IndexedMap<>(t0, t1, it);
        }
    }

    Map<PopCount, ? extends ForkJoinTask<IndexedMap<EntryTable>>>
    t0Map(PopCount pop, RingEntry r2) {

            PopCount pop2 = pop.sub(r2.pop);
            assert pop2!=null : "lePop underflow";

            EntryTable t0 = partitions.get(pop2).lePop;

            if(t0.isEmpty())
                return Collections.emptyMap();

            Map<PopCount, T0Builder> clops = new HashMap<>();

            for (RingEntry r0 : t0) {
                if(r0.index()>r2.index())
                    break;

                PopCount pop1 = pop2.sub(r0.pop);
                assert pop1!=null : "lePop underflow";

                int msk = r2.mlt20s(r0);
                Partition part = partitions.get(pop1).get(msk);
                RingEntry rad = r2.radials().and(r0.radials());

                int size = part.size();
                for(int idx = part.tail(rad); idx<size; ++idx) {
                    RdClop rdc = part.rdc(idx);

                    if(!rdc.radials.equals(rad))
                        break;

                    int key = RingEntry.MAX_INDEX * part.etx(idx) + r0.index;
                    clops.computeIfAbsent(rdc.clop, T0Builder::new).keyset.accept(key);
                }
            }

            ForkJoinTask.invokeAll(clops.values());

            return  clops;
        }

}
