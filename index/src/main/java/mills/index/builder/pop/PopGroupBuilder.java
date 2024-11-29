package mills.index.builder.pop;

import mills.bits.PopCount;
import mills.index.builder.GroupBuilder;
import mills.index.builder.T0Builder;
import mills.index.fragments.Partition;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import static mills.util.Indexed.max;

public class PopGroupBuilder extends GroupBuilder {

    final Partitions partitions;

    PopGroupBuilder(Partitions partitions, PopCount pop) {
        super(pop, partitions.minPops.get(pop));
        this.partitions = partitions;

        buildEntries();
    }

    private void buildEntries() {
        T0Builder builder = new T0Builder(this);
        partitions.pool.invoke(builder);
    }

    protected EntryTable t0(RingEntry r2) {
        PopCount pop0 = pop.sub(r2.pop);

        if (pop0.sum() > 16)
            return EntryTable.empty(); // won't fit into two rings

        return partitions.lePops.get(pop0).tailSet(r2);
    }

    protected Partition partition(RingEntry r2, RingEntry r0) {
        PopCount pop1 = pop.sub(r2.pop).sub(r0.pop);

        if(pop1.sum()>8)
            return Partition.empty();

        return partitions.get(pop1);
    }

    static PopGroupBuilder jumping(Partitions partitions, PopCount pop) {
        return new PopGroupBuilder(partitions, pop) {
            RingEntry limit(RingEntry r2, RingEntry r0) {
                RingEntry limit = max(r2, r0);
                if (r0.min() < limit.index)
                    limit = r0;

                return limit;
            }
        };
    }

    static PopGroupBuilder create(Partitions partitions, PopCount pop, boolean jump) {
        if(jump)
            return jumping(partitions, pop);
        else
            return new PopGroupBuilder(partitions, pop);
    }
}
