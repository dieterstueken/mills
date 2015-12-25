package mills.index4;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/15/15
 * Time: 6:52 PM
 */

/**
 * Class Partition is a lookup table for groups of
 * EntryTables of constant pop count matching a given msk
 */
public class Partitions {

    final EntryTables registry;

    final List<PartitionTable> partitions;

    Partitions(EntryTables registry) {
        this.registry = registry;
        this.partitions = Tasks.computeAll(PopCount.TABLE, this::partitionTable);
    }

    private PartitionTable partitionTable(PopCount pop) {

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

    private final List<Predicate<RingEntry>> filters = AbstractRandomList.generate(128, msk -> e -> e.stable(2 * msk));

    Partition partition(EntryTable parent, int mlt) {

        EntryTable root = registry.table(parent.filter(filters.get(mlt)));

        if (root.isEmpty())
            return Partition.EMPTY;

        return null;
    }

}
