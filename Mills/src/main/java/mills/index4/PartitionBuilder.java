package mills.index4;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.IndexedMap;
import mills.ring.RingEntry;
import mills.util.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
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

    final ForkJoinTask<Partitions> ptask;

    public PartitionBuilder(EntryTables registry) {
        this.registry = registry;
        ptask = submit(this::partitions);
    }

    public PartitionBuilder() {
        this(new EntryTables());
    }


    static <T> ForkJoinTask<T> submit(Callable<T> compute) {
        return ForkJoinTask.adapt(compute).fork();
    }

    static <T> List<T> joinAll(List<? extends ForkJoinTask<T>> tasks) {
        return AbstractRandomList.map(tasks, ForkJoinTask::join);
    }

    static <T, R> List<R> computeAll(Collection<T> src, Function<? super T, R> compute) {

        return joinAll(src.stream().
                map(t -> submit(() -> compute.apply(t))).
                collect(Collectors.toList()));
    }

    EntryTable entryTable(List<RingEntry> entryList) {
        return registry.table(entryList);
    }

    private Partitions partitions() {
        List<PartitionTable> tables = computeAll(PopCount.TABLE, this::partitionTable);
        return new Partitions(registry, tables);
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
                ForkJoinTask<Partition> task = submit(() -> partition(root, index));
                taskset.add(task);
                return task;
            }
        }).forEach(tasks::add);


        EntryTable lePop = pop.min() < 8 ? RingEntry.TABLE.filter(pop.le) : RingEntry.TABLE;

        taskset.removeIf(task -> task.join().root.isEmpty());

        final List<Partition> pset = joinAll(taskset);
        final List<Partition> partitions = joinAll(tasks);

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
                int cmp = rad.radials().compareTo(rdc.radials);
                if (cmp < 0)
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
                    RdClop rdc = RdClop.TABLE.get(key);
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

    class T0Builder extends RecursiveTask<IndexedMap<EntryTable>> {

        T0Builder(Object dummy) {
        }

        final IntStream.Builder keyset = IntStream.builder();

        RingEntry entry(int key) {
            return RingEntry.TABLE.get(key % RingEntry.MAX_INDEX);
        }

        @Override
        protected IndexedMap<EntryTable> compute() {
            int keys[] = keyset.build().toArray();
            EntryTable t0 = registry.table(AbstractRandomArray.virtual(keys.length, i -> entry(keys[i])));

            short s1[] = new short[keys.length];
            for (int i = 0; i < keys.length; ++i)
                s1[i] = (short) (keys[i] / RingEntry.MAX_INDEX);

            List<EntryTable> t1 = AbstractRandomArray.virtual(keys.length, i -> registry.get(s1[i]));
            IndexTable it = IndexTable.sum(t1, EntryTable::size);
            return new IndexedMap<>(t0, t1, it);
        }
    }

    Map<PopCount, T0Builder> t0Map(PopCount pop, RingEntry r2) {

        PopCount pop2 = pop.sub(r2.pop);
        assert pop2 != null : "lePop underflow";
        Partitions partitions = ptask.join();
        EntryTable t0 = partitions.get(pop2).lePop;
        if (t0.isEmpty())
            return Collections.emptyMap();
        Map<PopCount, T0Builder> clops = new HashMap<>();

        for (RingEntry r0 : t0) {
            if (r0.index() > r2.index())
                break;
            PopCount pop1 = pop2.sub(r0.pop);
            assert pop1 != null : "lePop underflow";
            int msk = r2.mlt20s(r0);
            Partition part = partitions.get(pop1).get(msk);
            RingEntry rad = r2.radials().and(r0.radials());

            part.process(rad, index -> {
                RdClop rdc = RdClop.TABLE.get(index/RDC);
                // compose new key from lower part of index and r0.index
                int key = RingEntry.MAX_INDEX * (index%RDC) + r0.index;
                clops.computeIfAbsent(rdc.clop, T0Builder::new).keyset.accept(key);
            });
        }

        ForkJoinTask.invokeAll(clops.values());

        return clops;
    }

    class T2Builder extends RecursiveTask<IndexedMap<IndexedMap<EntryTable>> > {

        T2Builder(Object dummy) {
        }

        List<RingEntry> t2 = new ArrayList<>();
        List<IndexedMap<EntryTable>> t0  = new ArrayList<>();

        void add(RingEntry e2, IndexedMap<EntryTable> e0) {
            if(!e0.isEmpty()) {
                t2.add(e2);
                t0.add(e0);
            }
        }

        @Override
        public IndexedMap<IndexedMap<EntryTable>> compute() {
            return new IndexedMap<>(EntryTable.of(t2), t0, IndexedMap::range);
        }

    }

    public ClopIndex index(PopCount pop) {

        Map<PopCount, T2Builder> builders = new HashMap<>();

        RingEntry.TABLE.forEach(e2 ->
            t0Map(pop, e2).entrySet().stream().forEach(e ->
                builders.computeIfAbsent(e.getKey(), T2Builder::new).add(e2, e.getValue().join())
            )
        );

        Map<PopCount, R2Index> subsets = new HashMap<>();

        builders.forEach((clop, i2) -> subsets.put(clop, new R2Index(i2.join(), pop)));

        return new ClopIndex(pop, subsets);
    }

    public static void main(String... args) {

        final EntryTables registry = new EntryTables();

        PartitionBuilder builder = new PartitionBuilder(registry);

        Partitions partitions = builder.partitions();

        System.out.println("done");

        IdentityHashMap<EntryTable, EntryTable> tables = new IdentityHashMap<>();

        Stat sizes = new Stat();
        Stat groups = new Stat();

        int count = 0;
        int masks = 0;
        for (PopCount pop : PopCount.TABLE) {
            PartitionTable table = partitions.get(pop);
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
