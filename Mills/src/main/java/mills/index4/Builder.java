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
public class Builder {

    final EntryTables registry;

    final ForkJoinTask<Partitions> partitions = submit(this::partitions);

    public Builder(EntryTables registry) {
        this.registry = registry;
    }

    public Builder() {
        this(new EntryTables());
    }

    EntryTable entryTable(List<RingEntry> entryList) {
        return registry.table(entryList);
    }

    private Partitions partitions() {
        List<PartitionTable> tables = computeAll(PopCount.TABLE, this::partitionTable);
        return new Partitions(tables);
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

        EntryTable lePop = RingEntry.TABLE.filter(pop.le);

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
            EntryTable table = registry.table(entry.getValue());
            return partition(root, RdClop.TABLE.get(rdci), table);
        }

        int index[] = new int[count];

        count = 0;
        for (Map.Entry<RdClop, List<RingEntry>> entry : tables.entrySet()) {
            int key = registry.index(entry.getValue());
            assert key >= 0;

            key += entry.getKey().index()*Short.MAX_VALUE;
            index[count++] = key;
        }

        return partition(root, registry, index);
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
            public int index(RingEntry rad) {
                int index = Arrays.binarySearch(keys, RdClop.index(rad, PopCount.EMPTY)*Short.MAX_VALUE);
                return index;
            }
        };
    }

    static Partition partition(EntryTable root, RdClop rdc, EntryTable table) {

        return new Partition(root) {
            @Override
            public int size() {
                return 1;
            }

            @Override
            public EntryTable get(int index) {
                if(index!=0)
                    throw new IndexOutOfBoundsException();

                return table;
            }

            @Override
            public RdClop rdc(int index) {
                return rdc;
            }

            @Override
            public int index(RingEntry rad) {
                int index = rad.radials().compareTo(rdc.radials);
                return index<1 ? index : -2;
            }

        };
    }

    private Map<PopCount, IntStream.Builder> r2List(PopCount pop, RingEntry r2) {

        PopCount pop2 = pop.sub(r2.pop);
        assert pop2!=null : "lePop underflow";

        Partitions partitions = this.partitions.join();

        EntryTable t0 = partitions.get(pop2).lePop;

        if(t0.isEmpty())
            return Collections.emptyMap();

        Map<PopCount, IntStream.Builder> clops = new HashMap<>();

        for (RingEntry r0 : t0) {
            if(r0.index()>r2.index())
                break;

            PopCount pop1 = pop2.sub(r0.pop);
            assert pop1!=null : "lePop underflow";

            int msk = r2.mlt20s(r0);
            Partition part = partitions.get(pop1).get(msk);
            RingEntry rad = r2.radials().and(r0.radials());

            int idx = part.index(rad);
            if(idx<0)
                idx = -1-idx;

            for(int n = part.size(); idx<n; ++idx) {
                RdClop rdc = part.rdc(idx);
                if(!rdc.radials.equals(rad))
                    break;

                int key = r0.index;
                key = 128*key + idx;
                key = 128*key + msk;
                key =  25*key + pop1.index;

                clops.computeIfAbsent(rdc.clop, clop-> IntStream.builder()).accept(key);
            }
        }

        return clops;
    }

    static interface T1 extends List<EntryTable> {
        EntryTable t0();
        int offset(int index);
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

        Builder builder = new Builder(registry);

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
