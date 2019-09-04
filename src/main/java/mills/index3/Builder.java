package mills.index3;

import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.index1.IndexList;
import mills.index3.partitions.ClopTable;
import mills.index3.partitions.MaskTable;
import mills.index3.partitions.Partitions;
import mills.index3.partitions.RadialTable;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.IndexedMap;
import mills.ring.RingEntry;
import mills.util.AbstractRandomArray;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/29/15
 * Time: 6:59 PM
 */
public class Builder {

    protected final EntryTables registry;

    protected final Partitions partitions;

    public Builder(EntryTables registry) {
        this.registry = registry;
        partitions = Partitions.build(registry);
    }

    public Builder() {
        this(new EntryTables());
    }

    public MaskTable table(PopCount pop) {
        return partitions.get(pop.index);
    }

    public EntryTable lePop(PopCount pop) {
        return partitions.lePop(pop);
    }

    class T1Builder extends RecursiveTask<IndexedMap<EntryTable>> {

        final RingEntry e2;

        final PopCount clop;

        T1Builder(RingEntry e2, PopCount clop) {
            this.clop = clop;
            this.e2 = e2;
        }

        List<RingEntry> l0 = new ArrayList<>();
        List<EntryTable> l1 = new ArrayList<>();

        @Override
        protected IndexedMap<EntryTable> compute() {

            EntryTable t0 = EntryTable.of(l0);
            List<EntryTable> t1 = registry.build(l1);

            l0.clear();
            l1.clear();

            return new IndexedMap<>(t0, t1, EntryTable::size);
        }

        public void accept(RingEntry e0, EntryTable t1) {
            assert l0.isEmpty() || l0.get(l0.size()-1).index < e0.index;
            this.l0.add(e0);
            this.l1.add(t1);
        }
    }

    class T0Builder extends RecursiveTask<List<T1Builder>> {

        final Map<PopCount, T1Builder> clops = new HashMap<>();
        final PopCount pop2;
        final RingEntry e2;

        T0Builder(PopCount pop, RingEntry e2) {
            this.pop2 = pop.sub(e2.pop);;
            assert pop2 != null : "lePop underflow";
            this.e2 = e2;
        }

        void add(RingEntry r0) {
            PopCount pop1 = pop2.sub(r0.pop);
            assert pop1 != null : "lePop underflow";
            int msk = e2.mlt20s(r0);
            RadialTable part = table(pop1).get(msk);
            RingEntry rad = e2.radials().and(r0.radials());
            ClopTable clops = part.get(rad);
            PopCount clop = e2.clop().add(r0.clop());

            clops.forEach(t1 -> get(clop(clop, rad, t1)).accept(r0, t1));
        }

        PopCount clop(PopCount clop, RingEntry rad, EntryTable t1) {
            // peek first entry
            RingEntry e1 = t1.get(0);
            clop = clop.add(e1.clop());

            // add actual clop for current rad from first entry
            clop = clop.add(e1.radials().and(rad).pop());
            return clop;
        }

        T1Builder get(PopCount clop) {
            return clops.computeIfAbsent(clop, this::t1Builder);
        }

        T1Builder t1Builder(PopCount clop) {
            return new T1Builder(e2, clop);
        }


        @Override
        protected List<T1Builder> compute() {

            EntryTable t0 = lePop(pop2);
            if (t0.isEmpty())
                return Collections.emptyList();

            for (RingEntry r0 : t0) {
                if (r0.index() > e2.index())
                    break;

                if(r0.index==3280)
                    if(e2.index==6560)
                        r0.radix();

                add(r0);
            }

            List<T1Builder> builders = new ArrayList<>(clops.values());
            clops.clear();  // drop intermediate results

            ForkJoinTask.invokeAll(builders);
            return builders;
        }
    }

    // accumulates entries for a given clop
    class T2Builder extends RecursiveTask<IndexedMap<IndexedMap<EntryTable>> > {

        final PopCount pop;
        final PopCount clop;

        T2Builder(PopCount pop, PopCount clop) {
            this.pop = pop;
            this.clop = clop;
        }

        List<RingEntry> t2 = new ArrayList<>();
        List<IndexedMap<EntryTable>> t0  = new ArrayList<>();

        void add(RingEntry e2, IndexedMap<EntryTable> e0) {
            if(!e0.isEmpty()) {
                t2.add(e2);
                t0.add(e0);
            }
        }

        void add(T1Builder t1b) {
            final IndexedMap<EntryTable> index = t1b.join();
            add(t1b.e2, index);
        }

        @Override
        public IndexedMap<IndexedMap<EntryTable>> compute() {
            return new IndexedMap<>(EntryTable.of(t2), t0, IndexedMap::range);
        }

        R2Index index() {
            return new R2Index(join(), pop);
        }
    }

    Collection<T2Builder> t2Builders(final PopCount pop) {

        List<T2Builder> builders = new HashMap<PopCount, T2Builder>() {

            List<T0Builder> builders() {
                return AbstractRandomArray.map(lePop(pop), this::t0Builder);
            }

            {
                ForkJoinTask.invokeAll(builders()).forEach(this::add2);
            }

            void add2(T0Builder t0b) {
                t0b.join().forEach(this::add1);
            }

            void add1(T1Builder t1b) {
                computeIfAbsent(t1b.clop, this::t2Builder).add(t1b);
            }

            T2Builder t2Builder(PopCount clop) {
                return new T2Builder(pop, clop);
            }

            T0Builder t0Builder(RingEntry e2) {
                return new T0Builder(pop, e2);
            }

        }.values().stream().collect(Collectors.toList());

        ForkJoinTask.invokeAll(builders);

        return builders;
    }

    ClopIndex index(PopCount pop) {

        Map<PopCount, PosIndex> subsets = new HashMap<>();

        t2Builders(pop).forEach(t2b->subsets.put(t2b.clop, t2b.index()));

        return new ClopIndex(pop, subsets::get);
    }

    public static void main(String... args) throws IOException {

        System.in.read();

        Builder builder = new Builder();

        System.out.format("start [%d]\n", builder.registry.count());

        final PopCount pop = PopCount.of(8, 8);
        ClopIndex index = builder.index(pop);

        System.out.format("%s %d\n", index.pop, index.range());

        IndexList indexes = IndexList.create();

        PosIndex index2 = indexes.get(pop);

        index2.range();

        System.out.format("%s %d\n", index2.pop(), index2.range());

        System.out.format("clops %d\n", index.clops.size());

    }
}
