package mills.index4;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.IndexedMap;
import mills.ring.RingEntry;

import java.util.Collections;
import java.util.HashMap;
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

        final IntStream.Builder keys = IntStream.builder();

        @Override
        protected IndexedMap<EntryTable> compute() {
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

                    int key = r0.index;
                    key = 128*key + idx;
                    key = 128*key + msk;
                    // wrong: 100
                    key =  25*key + pop1.index;

                    clops.computeIfAbsent(rdc.clop, T0Builder::new).keys.accept(key);
                }
            }

            ForkJoinTask.invokeAll(clops.values());

            return  clops;
        }

}
