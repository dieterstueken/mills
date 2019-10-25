package mills.partitions;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomArray;
import mills.util.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.04.16
 * Time: 10:23
 */
public class PartitionBuilder<P> extends RecursiveTask<Partitions<P>> {

    final List<Predicate<RingEntry>> filters = AbstractRandomArray.generate(128, msk -> entry -> entry.stable(msk));

    final Function<EntryTable, P> generator;

    final PartitionTable<P> empty ;

    public PartitionBuilder(Function<EntryTable, P> generator) {
        this.generator = generator;
        this.empty = PartitionTable.empty(generator.apply(EntryTable.EMPTY));
    }

    public PartitionTable<P> build(PopCount pop) {

        if(pop.sum()>8)
            return empty;

        EntryTable root = Entries.MINIMIZED.filter(pop.eq);

        if(root.isEmpty())
            return empty;

        ForkJoinTask<EntryTable> lePopTask = ForkJoinTask.adapt(()-> Entries.TABLE.filter(pop.le)).fork();

        final Set<PGroup> groups = PGroup.groups(root);

        List<P> partitions = new ArrayList<>(20);
        List<P> table = new ArrayList<>(128);

        IntFunction<P> get = msk -> {
            // get lowest part mask which may have been already calculated.
            int lix = PGroup.lindex(groups, msk);
            if(lix<msk)
                return table.get(lix);

            P partition = generator.apply(root.filter(filters.get(msk)));
            partitions.add(partition);
            return partition;
        };

        // populate all partitions
        for(int msk=0; msk<128; ++msk) {
            table.add(get.apply(msk));
        }

        return PartitionTable.of(partitions, table, lePopTask.join());
    }

    public Partitions<P> compute() {

        List<PartitionTable<P>> partitions = Tasks.computeAll(PopCount.TABLE, this::build);

        return new Partitions<P>() {

            @Override
            public PartitionTable<P> get(int index) {
                return partitions.get(index);
            }
        };
    }
}
