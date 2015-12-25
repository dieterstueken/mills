package mills.index4;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.index.IndexList;
import mills.index.PosIndex;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.IndexedMap;
import mills.ring.RingEntry;
import mills.util.*;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/15/15
 * Time: 7:22 PM
 */
public class PartitionBuilder {

    final EntryTables registry;

    final List<PartitionTable> partitions;

    public PartitionBuilder(EntryTables registry) {
        this.registry = registry;
        this.partitions = Tasks.computeAll(PopCount.TABLE, this::partitionTable);
    }

    public PartitionBuilder() {
        this(new EntryTables());
    }

    EntryTable entryTable(List<RingEntry> entryList) {
        return registry.table(entryList);
    }

    PartitionTable table(PopCount pop) {
        return partitions.get(pop.index);
    }

    PartitionTable partitionTable(PopCount pop) {

        EntryTable root = pop.sum() > 8 ? EntryTable.EMPTY : RingEntry.MINIMIZED.filter(pop.eq);

        if (root.isEmpty())
            return PartitionTable.EMPTY;

        Set<PGroup> groups = PGroup.groups(root);
        final List<ForkJoinTask<Partition>> tasks = new ArrayList<>(128);
        final List<ForkJoinTask<Partition>> taskset = new ArrayList<>(1 << groups.size());

        IntStream.range(0, 128).mapToObj(index -> {

            assert index == tasks.size();

            int lindex = PGroup.lindex(groups, index);
            if (lindex < index) {
                // repeat previous value
                return tasks.get(lindex);
            } else {
                ForkJoinTask<Partition> task = Tasks.submit(() -> partition(root, index));
                taskset.add(task);
                return task;
            }
        }).forEach(tasks::add);


        EntryTable lePop = pop.min() < 8 ? RingEntry.TABLE.filter(pop.le) : RingEntry.TABLE;

        taskset.removeIf(task -> task.join().root.isEmpty());

        final List<Partition> pset = Tasks.joinAll(taskset);
        final List<Partition> partitions = Tasks.joinAll(tasks);

        return new PartitionTable(root, lePop, partitions, pset);
    }

    final List<Predicate<RingEntry>> filters = AbstractRandomList.generate(128, msk -> e -> e.stable(2 * msk));

    static final int RDC = 1 << 16;

    Partition partition(EntryTable parent, int mlt) {

        EntryTable root = entryTable(parent.filter(filters.get(mlt)));

        if (root.isEmpty())
            return Partition.EMPTY;

        Map<RdClop, List<RingEntry>> tables = new TreeMap<RdClop, List<RingEntry>>(RdClop.CMP) {

            void add(RingEntry entry) {
                RdClop.of(entry).subsets()
                        .map(RdClop::closed)
                        .filter(Objects::nonNull)
                        .forEach(rdc -> computeIfAbsent(rdc, rdClop -> new ArrayList<>()).add(entry));
            }

            {
                root.forEach(this::add);
            }
        };

        int count = tables.size();

        if (count == 1) {
            Map.Entry<RdClop, List<RingEntry>> entry = tables.entrySet().iterator().next();

            int rdci = entry.getKey().index();
            short etx = registry.key(entry.getValue());

            //  single entry: must be complete root
            assert root.equals(registry.get(etx));

            return partition(root, rdci, etx);
        }

        int index[] = new int[count];

        count = 0;
        for (Map.Entry<RdClop, List<RingEntry>> entry : tables.entrySet()) {

            int rdci = entry.getKey().index();
            int etx = registry.key(entry.getValue());

            assert etx >= 0; // not empty

            index[count++] = rdci * RDC + etx;
        }

        return partition(root, index);
    }

    Map.Entry<RdClop, EntryTable> pentry(int etx) {
        return pentry(etx / RDC, etx % RDC);
    }

    Map.Entry<RdClop, EntryTable> pentry(int rdci, int etx) {
        RdClop key = RdClop.TABLE.get(rdci);
        EntryTable table = registry.get(etx);
        return new AbstractMap.SimpleImmutableEntry<>(key, table);
    }

    Partition partition(EntryTable root, int rdci, short etx) {

        RdClop rdc = RdClop.TABLE.get(rdci);

        return new Partition(root) {

            @Override
            public int size() {
                return 1;
            }

            @Override
            public Set<Entry<RdClop, EntryTable>> entrySet() {
                return Collections.singleton(pentry(rdci, etx));
            }

            @Override
            public void process(RingEntry rad, IntConsumer consumer) {
                if (rad.radials().equals(rdc.radials))
                    consumer.accept(rdci * RDC + etx);
            }
        };
    }

    Partition partition(EntryTable root, int keys[]) {

        return new Partition(root) {

            @Override
            public int size() {
                return keys.length;
            }

            @Override
            public Set<Entry<RdClop, EntryTable>> entrySet() {
                return new ListSet<Entry<RdClop, EntryTable>>() {

                    @Override
                    public Entry<RdClop, EntryTable> get(int index) {
                        return pentry(keys[index]);
                    }

                    @Override
                    public int size() {
                        return keys.length;
                    }
                };
            }

            @Override
            public void process(RingEntry rad, IntConsumer consumer) {

                for (int index = start(rad); index < size(); ++index) {
                    int key = keys[index];

                    // verify if within chunk of matching radials
                    RdClop rdc = RdClop.TABLE.get(key/RDC);
                    if (!rdc.radials.equals(rad))
                        break;

                    consumer.accept(key);
                }
            }

            public int start(RingEntry rad) {
                int index = Arrays.binarySearch(keys, RDC * RdClop.index(rad, PopCount.EMPTY));
                return index < 0 ? -1 - index : index;
            }
        };
    }

    class T1Builder extends RecursiveTask<IndexedMap<EntryTable>> {

        final RingEntry e2;

        final PopCount clop;

        T1Builder(RingEntry e2, PopCount clop) {
            this.clop = clop;
            this.e2 = e2;
        }

        IntStream.Builder keyset = IntStream.builder();

        RingEntry entry(int key) {
            return RingEntry.TABLE.get(key % RingEntry.MAX_INDEX);
        }

        @Override
        protected IndexedMap<EntryTable> compute() {
            int keys[] = keyset.build().toArray();
            keyset = null;

            EntryTable t0 = registry.table(AbstractRandomArray.virtual(keys.length, i -> entry(keys[i])));

            short s1[] = new short[keys.length];
            for (int i = 0; i < keys.length; ++i)
                s1[i] = (short) (keys[i] / RingEntry.MAX_INDEX);

            List<EntryTable> t1 = AbstractRandomArray.virtual(keys.length, i -> registry.get(s1[i]));
            return new IndexedMap<>(t0, t1, EntryTable::size);
        }

        public void accept(int key) {
            keyset.accept(key);
        }
    }

    List<T1Builder> t1Builders(PopCount pop, RingEntry e2) {

        PopCount pop2 = pop.sub(e2.pop);
        assert pop2 != null : "lePop underflow";
        EntryTable t0 = table(pop2).lePop;
        if (t0.isEmpty())
            return Collections.emptyList();

        Map<PopCount, T1Builder> clops = new HashMap<>();

        if(e2.index==81)
            e2.hashCode();

        for (RingEntry r0 : t0) {
            if (r0.index() > e2.index())
                break;
            PopCount pop1 = pop2.sub(r0.pop);
            assert pop1 != null : "lePop underflow";
            int msk = e2.mlt20s(r0);
            Partition part = table(pop1).get(msk);
            RingEntry rad = e2.radials().and(r0.radials());

            part.process(rad, index -> {
                RdClop rdc = RdClop.TABLE.get(index/RDC);
                // compose new key from lower part of index and r0.index
                int key = RingEntry.MAX_INDEX * (index%RDC) + r0.index;
                clops.computeIfAbsent(rdc.clop, clop -> new T1Builder(e2, clop)).accept(key);
            });
        }

        return new ArrayList<>(clops.values());
    }

    class T0Builder extends RecursiveTask<List<T1Builder>> {

        final PopCount pop;
        final RingEntry e2;

        T0Builder(PopCount pop, RingEntry e2) {
            this.pop = pop;
            this.e2 = e2;
        }

        @Override
        protected List<T1Builder> compute() {
            List<T1Builder> result = t1Builders(pop, e2);
            ForkJoinTask.invokeAll(result);
            return result;
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
                return AbstractRandomArray.map(table(pop).lePop, this::t0Builder);
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

        return new ClopIndex(pop, subsets);
    }

    public static void main(String... args) {
        PartitionBuilder builder = new PartitionBuilder();

        final PopCount pop = PopCount.of(2, 0);
        ClopIndex index = builder.index(pop);

        System.out.format("%s %d\n", index.pop, index.range());

        IndexList indexes = IndexList.create();

        PosIndex index2 = indexes.get(pop);

        System.out.format("%s %d\n", index2.pop(), index2.range());
    }

    public static void _main(String... args) {

        final EntryTables registry = new EntryTables();

        PartitionBuilder builder = new PartitionBuilder(registry);

        System.out.println("done");

        IdentityHashMap<EntryTable, EntryTable> tables = new IdentityHashMap<>();

        Stat sizes = new Stat();
        Stat groups = new Stat();

        int count = 0;
        int masks = 0;
        for (PopCount pop : PopCount.TABLE) {
            PartitionTable table = builder.table(pop);
            masks += table.pset.size();
            for (Partition partition : table.pset) {

                sizes.accept(partition.size());
                groups.accept(groups(partition));

                for (EntryTable p : partition.values()) {
                    if (p.size() > 1) {
                        tables.put(p, p);
                        ++count;
                        registry.table(p);
                    }
                }
            }
        }

        System.out.format("total: %d / %d / %d / %d\n", masks, count, tables.size(), registry.count());

        sizes.dump("sizes");
        groups.dump("groups");
    }

    static int groups(Partition p) {
        Set<RdClop> rdcs = new HashSet<>();
        p.root.forEach(e -> rdcs.add(RdClop.of(e)));
        return rdcs.size();
    }
}
