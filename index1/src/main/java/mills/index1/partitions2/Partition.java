package mills.index1.partitions2;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/21/14
 * Time: 9:14 AM
 */
public class Partition extends AbstractRandomList<PartitionGroup> {

    final List<EntryTable> tables;

    final List<PartitionGroup> groups; // 128

    final List<PartitionGroup> set;

    private Partition(List<EntryTable> tables, List<PartitionGroup> groups, List<PartitionGroup> set) {
        this.tables = tables;
        this.groups = groups;
        this.set = set;

        assert groups.size() == 128;
    }

    public static Partition EMPTY = new Partition(
            Collections.emptyList(),
            AbstractRandomList.constant(128, PartitionGroup.EMPTY),
            Collections.emptyList()
    ) {
        @Override
        public int getKey(int msk, PopCount clop, int radials) {
            return 0;
        }

        @Override
        public String toString() {
            return "empty";
        }
    };

    @Override
    public int size() {
        return 128;
    }

    @Override
    public PartitionGroup get(int index) {
        return groups.get(index);
    }

    public int getKey(int msk, PopCount clop, int radials) {
        return groups.get(msk).getKey(clop, radials);
    }

    int count() {
        int count = 0;
        for (PartitionGroup pg : set) {
            count += pg.count();
        }
        return count;
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    static Partition build(PopCount pop) {
        return new Partition.Builder().partition(pop);
    }

    static class Builder {

        final List<EntryTable> tables = new ArrayList<>();

        final Map<EntryTable, Integer> tmap = new TreeMap<>(Entries.BY_SIZE);

        Partition partition(PopCount pop) {
            tables.clear();
            tmap.clear();

            EntryTable root = Entries.MINIMIZED.filter(pop.eq);
            if(root.isEmpty())
                return Partition.EMPTY;

            PartitionGroup groups[] = new PartitionGroup[128];
            List<PartitionGroup> gset = new LinkedList<>();

            /**
             * The groups[128] contain only a few different entries.
             * PGroup.Set maps some index to an unique index with an equivalent entry which may be copied.
             */

            final Set<PGroup> pset = PGroup.groups(root);

            // populate all partitions2
            for (int msk = 0; msk < 128; ++msk) {

                PartitionGroup group = groups[msk];
                if(group!=null) // already done
                    continue;

                // try get an entry which may have been calculated before.
                int part = PGroup.pindex(pset, msk);

                group = groups[part];
                if (group == null) {
                    // generate a new partition
                    group = PartitionGroup.build(pop, root.filter(filter(msk)), this::allocateKey);
                    groups[part] = group;
                    if(!group.isEmpty())
                        gset.add(group);
                }

                // populate entry
                if (part != msk)
                    groups[msk] = group;
            }

            return new Partition(List.copyOf(tables), List.of(groups), List.copyOf(gset));
        }

        private  Predicate<RingEntry> filter(int msk) {
            return e->(e.mlt & (2*msk)) == 0;
        }

        private Integer allocateKey(EntryTable table) {
            if(table.isEmpty())
                return 0;

            if(table.size()==1)
                return -1-table.get(0).index;

            Integer key = tmap.get(table);
            if(key==null) {
                key = tables.size()+1;
                tables.add(table);
                tmap.put(table, key);
            }

            return key;
        }
    }
}
