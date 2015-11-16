package mills.index4;

import com.google.common.collect.ImmutableList;
import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
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

    final Function<List<RingEntry>, EntryTable> tableBuilder;

    EntryTable entryTable(List<RingEntry> entryList) {
        return tableBuilder.apply(entryList);
    }

    public Builder(Function<List<RingEntry>, EntryTable> tableBuilder) {
        this.tableBuilder = tableBuilder;
    }

    public Builder() {
        this(EntryTable::of);
    }

    public Partitions partitions() {
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

    public static final Comparator<RingEntry> RDC_ORDER = Comparator.comparing(RdClop::of, Comparator.nullsFirst((RdClop.CMP)));

    Partition partition(EntryTable parent, int mlt) {

        EntryTable root = entryTable(parent.filter(filters.get(mlt)));

        if(root.isEmpty())
            return Partition.EMPTY;

        List<RingEntry> entries = new ArrayList<>(root);
        Collections.sort(entries, RDC_ORDER);

        List<EntryTable> tables = new ArrayList<>();

        while(!entries.isEmpty()) {
            int l = entries.size();
            RdClop rdc = RdClop.of(entries.get(l-1));
            int i = l-1;
            while(i>=0 && rdc.equals(RdClop.of(entries.get(i)))) {
                --i;
            }

            // i+1 was the last valid index
            List<RingEntry> chunk = entries.subList(i+1, l);
            tables.add(0, entryTable(chunk));
            chunk.clear();
        }

        if(tables.isEmpty())
            return Partition.EMPTY;

        if(tables.size()==1) {
            return partition(root, tables.get(0));
        }

        return partition(root, ImmutableList.copyOf(tables));
    }

    public static RdClop rdc(EntryTable table) {
        return table==null || table.isEmpty() ? null : RdClop.of(table.get(0));
    }

    static Partition partition(EntryTable root, List<EntryTable> tables) {

        List<RdClop> index = AbstractRandomList.map(tables, Builder::rdc);

        return new Partition(root) {

            @Override
            public List<EntryTable> tables() {
                return tables;
            }

            @Override
            public EntryTable get(RdClop rdc) {
                int i = Collections.binarySearch(index, rdc, RdClop.CMP);
                return i<0 ? EntryTable.EMPTY : tables.get(i);
            }
        };
    }

    static Partition partition(EntryTable root, EntryTable table) {
        if(table.size()==1)
            return partition(root, table.get(0).index);

        return new Partition(root) {

            @Override
            public EntryTable get(RdClop rdc) {
                return rdc.equals(rdc(table)) ? table : EntryTable.EMPTY;
            }

            @Override
            public List<EntryTable> tables() {
                return Collections.singletonList(table);
            }
        };
    }

    static Partition partition(EntryTable root, short index) {

        return new Partition(root) {

            RingEntry entry() {
                return RingEntry.of(index);
            }

            @Override
            public EntryTable get(RdClop rdc) {
                return RdClop.of(entry()).equals(rdc) ? entry().singleton : EntryTable.EMPTY;
            }

            @Override
            public List<EntryTable> tables() {
                return Collections.singletonList(entry().singleton);
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
        Builder builder = new Builder();

        //builder.partitionTable(PopCount.of(0,0));

        Partitions partitions = builder.partitions();

        System.out.println("done");
    }
}
