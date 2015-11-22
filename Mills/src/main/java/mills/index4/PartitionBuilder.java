package mills.index4;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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

    final ForkJoinTask<Partitions> partitions = submit(this::partitions);

    public PartitionBuilder(EntryTables registry) {
        this.registry = registry;
    }

    public PartitionBuilder() {
        this(new EntryTables());
    }

    EntryTable entryTable(List<RingEntry> entryList) {
        return registry.table(entryList);
    }

    private Partitions partitions() {
        List<PartitionTable> tables = computeAll(PopCount.TABLE, this::partitionTable);
        return new Partitions(registry, tables);
    }

    PartitionTable partitionTable(PopCount pop) {

        EntryTable root = pop.sum()>8 ? EntryTable.EMPTY : RingEntry.MINIMIZED.filter(pop.eq);

        if(root.isEmpty())
            return PartitionTable.EMPTY;

        Set<PGroup> groups = PGroup.groups(root);
        final List<ForkJoinTask<Partition>> tasks = new ArrayList<>(128);
        final List<ForkJoinTask<Partition>> taskset = new ArrayList<>(1<<groups.size());

        IntStream.range(0, 128).mapToObj(index -> {

            assert index == tasks.size();

            int lindex = PGroup.lindex(groups, index);
            if (lindex < index) {
                // repeat previous value
                return tasks.get(lindex);
            } else {
                ForkJoinTask<Partition> task = submit(()-> partition(root, index));
                taskset.add(task);
                return task;
            }
        }).forEach(tasks::add);


        EntryTable lePop = pop.min()<8 ? RingEntry.TABLE.filter(pop.le) : RingEntry.TABLE;

        taskset.removeIf(task -> task.join().root.isEmpty());

        final List<Partition> pset = joinAll(taskset);
        final List<Partition> partitions = joinAll(tasks);

        return new PartitionTable(root, lePop, partitions, pset);
    }

    final List<Predicate<RingEntry>> filters = AbstractRandomList.generate(128, msk -> e -> e.stable(2 * msk));

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
            short etx = registry.index(entry.getValue());

            //  single entry: must be complete root
            assert root.equals(registry.get(etx));

            return partition(root, rdci, etx);
        }

        int index[] = new int[count];

        count = 0;
        for (Map.Entry<RdClop, List<RingEntry>> entry : tables.entrySet()) {

            int rdci = entry.getKey().index();
            int etx = registry.index(entry.getValue());

            assert etx>=0; // not empty

            index[count++] = rdci*Short.MAX_VALUE + etx;
        }

        return partition(root, registry, index);
    }

    static Partition partition(EntryTable root, int rdci, short etx) {

        RdClop rdc = RdClop.TABLE.get(rdci);

        return new Partition(root) {
            @Override
            public int size() {
                return 1;
            }

            @Override
            public EntryTable get(int index) {
                if(index!=0)
                    throw new IndexOutOfBoundsException();

                return root;
            }

            @Override
            public RdClop rdc(int index) {
                return rdc;
            }

            public short etx(int index) {
                if(index!=0)
                    throw new IndexOutOfBoundsException();

                return etx;
            }

            @Override
            public int tail(RingEntry rad) {
                int cmp = rad.radials().compareTo(rdc.radials);
                return cmp<=0 ? 0 : 1;
            }

        };
    }

    static Partition partition(EntryTable root, EntryTables registry, int keys[]) {

        return new Partition(root) {

            @Override
            public int size() {
                return keys.length;
            }

            @Override
            public EntryTable get(int index) {
                int key = keys[index]%Short.MAX_VALUE;
                return registry.get(key);
            }

            @Override
            public RdClop rdc(int index) {
                int key = keys[index]/Short.MAX_VALUE;
                return RdClop.TABLE.get(key);
            }

            @Override
            public int tail(RingEntry rad) {
                int index = Arrays.binarySearch(keys, RdClop.index(rad, PopCount.EMPTY)*Short.MAX_VALUE);
                return index<0 ? -1-index : index;
            }
        };
    }

    static <T> ForkJoinTask<T> submit(Callable<T> compute) {
        return ForkJoinTask.adapt(compute).fork();
    }

    static <T> List<T> joinAll(List<? extends ForkJoinTask<T>> tasks) {
        return AbstractRandomList.map(tasks, ForkJoinTask::join);
    }

    static <T,R> List<R> computeAll(Collection<T> src, Function<? super T, R> compute) {

        return joinAll(src.stream().
                map(t -> submit(() -> compute.apply(t))).
                collect(Collectors.toList()));
    }

    public static void main(String ... args) {

        final EntryTables registry = new EntryTables();

        PartitionBuilder builder = new PartitionBuilder(registry);

        Partitions partitions = builder.partitions();

        System.out.println("done");

        IdentityHashMap<EntryTable, EntryTable> tables = new IdentityHashMap<>();

        Map<Integer, AtomicInteger> stat = new TreeMap<>();
        int count=0;
        int masks = 0;
        for (PopCount pop : PopCount.TABLE) {
            PartitionTable table = partitions.get(pop);
            masks += table.pset.size();
            for (Partition partition : table.pset) {

                int n = partition.size();
                //int n = partition.root.stream().map(RdClop::of).collect(Collectors.toSet()).size();

                stat.computeIfAbsent(n, i -> new AtomicInteger()).incrementAndGet();

                for (EntryTable p : partition) {
                    if(p.size()>1) {
                        tables.put(p,p);
                        ++count;
                        registry.table(p);
                    }
                }
            }
        }

        System.out.format("total: %d / %d / %d / %d\n", masks, count, tables.size(), registry.count());

        for (Map.Entry<Integer, AtomicInteger> e : stat.entrySet()) {
            System.out.format("%d %4d\n", e.getKey(), e.getValue().get());
        }
    }
}
