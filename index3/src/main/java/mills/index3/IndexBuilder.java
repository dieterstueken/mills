package mills.index3;

import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.index3.partitions.MaskTable;
import mills.index3.partitions.Partitions;
import mills.index3.partitions.RadialTable;
import mills.ring.EntryTable;
import mills.ring.IndexedMap;
import mills.ring.RingEntry;
import mills.util.IndexTable;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 1/1/16
 * Time: 3:55 PM
 */
public class IndexBuilder extends RecursiveTask<ClopIndex> {

    protected final Partitions partitions;

    final PopCount pop;

    public IndexBuilder(Partitions partitions, PopCount pop) {
        this.partitions = partitions;
        this.pop = pop;
    }

    public MaskTable table(PopCount pop) {
        return partitions.get(pop.index);
    }

    public EntryTable lePop(PopCount pop) {
        return partitions.lePop(pop);
    }

    // build a single IndexMap(e0, t1)
    class E0Builder extends RecursiveTask<IndexedMap<EntryTable>> {

        final RingEntry e2;

        final PopCount clop;

        public E0Builder(RingEntry e2, PopCount clop) {
            this.e2 = e2;
            this.clop = clop;
        }

        final List<RingEntry> l0= new ArrayList<>();
        final List<EntryTable> l1 = new ArrayList<>();

        void add(RingEntry e0, EntryTable t1) {
            l0.add(e0);
            l1.add(t1);
        }

        @Override
        protected IndexedMap<EntryTable> compute() {
            final IndexTable it = IndexTable.sum(l1, EntryTable::size);
            final List<EntryTable> t1 = partitions.registry.register(l1);
            l1.clear();

            final EntryTable t0 = EntryTable.of(l0);
            l0.clear();

            return new IndexedMap<>(t0, t1, it);
        }
    }

    ForkJoinTask<List<E0Builder>> e0Builders(RingEntry e2) {
        PopCount pop2 = pop.sub(e2.pop);
        EntryTable t0 = lePop(pop2);
        if(t0.isEmpty())
            return null;

        return new RecursiveTask<List<E0Builder>>() {

            E0Builder builders[];

            E0Builder builder(PopCount clop) {
                int index = clop.index;
                E0Builder e0b = builders[index];
                if(e0b==null)
                    builders[index] = e0b = new E0Builder(e2, clop);
                return e0b;
            }

            void add(RingEntry e0) {
                PopCount pop1 = pop2.sub(e0.pop);
                assert pop1 != null : "lePop underflow";
                int msk = e2.mlt20s(e0);
                RadialTable part = table(pop1).get(msk);
                RingEntry rad20 = e2.radials().and(e0.radials());
                PopCount clop20 = e2.clop().add(e0.clop());
                for (EntryTable t1 : part.get(rad20)) {
                    // peek first entry
                    RingEntry e1 = t1.get(0);
                    PopCount clop = rad20.and(e1.radials()).pop()
                            .add(clop20)
                            .add(e1.clop());
                    builder(clop).add(e0, t1);
                }
            }

            @Override
            protected List<E0Builder> compute() {
                builders = new E0Builder[25];
                t0.forEach(this::add);

                ArrayList<E0Builder> list = new ArrayList<>(25);
                for (E0Builder b : builders) {
                    if(b!=null)
                        list.add(b);
                }

                builders = null;

                ForkJoinTask.invokeAll(list);

                return list;
            }
        };
    }

    class E2Builder extends RecursiveTask<PosIndex> {

        E2Builder(Object dummy) {

        }

        final List<RingEntry> l2= new ArrayList<>();
        final List<IndexedMap<EntryTable>> l0 = new ArrayList<>();

        void add(RingEntry e2, IndexedMap<EntryTable> t0) {
            l2.add(e2);
            l0.add(t0);
        }

        @Override
        protected PosIndex compute() {
            IndexedMap<IndexedMap<EntryTable>> m2 = new IndexedMap<>(EntryTable.of(l2), l0, IndexedMap::range);
            return new R2Index(m2, pop);
        }
    }

    protected ClopIndex compute() {

        Deque<ForkJoinTask<List<E0Builder>>> tasks = new ArrayDeque<>();

        lePop(pop).stream().map(this::e0Builders).filter(Objects::nonNull).forEach(tasks::add);

        tasks.descendingIterator().forEachRemaining(ForkJoinTask::fork);

        Map<PopCount, E2Builder> builders = new HashMap<>(25);

        Consumer<E0Builder> build = b -> builders.computeIfAbsent(b.clop, E2Builder::new).add(b.e2, b.join());

        tasks.stream().map(ForkJoinTask::join).flatMap(List::stream).forEach(build);

        ForkJoinTask.invokeAll(builders.values());

        return new ClopIndex(pop, clop -> builders.get(clop).join());

    }
}
