package mills.partitions;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/21/14
 * Time: 9:14 AM
 */
public class Partition {

    final List<EntryTable> tables;

    final List<PartitionGroup> groups;

    final List<PartitionGroup> set;

    public Partition(List<EntryTable> tables, List<PartitionGroup> groups, List<PartitionGroup> set) {
        this.tables = tables;
        this.groups = groups;
        this.set = set;
    }

    public int getKey(int msk, int clop, int radials) {
        return groups.get(msk).getKey(clop, radials);
    }

    public static Partition EMPTY = new Partition(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()) {

        @Override
        public int getKey(int msk, int clop, int radials) {
            return -1;
        }
    };

    public int count() {
        int count = 0;
        for (PartitionGroup pg : set) {
            count += pg.groups.size();
        }
        return count;
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    static class Builder {

        final List<EntryTable> tables = new ArrayList<>();

        final Map<EntryTable, Integer> tmap = new TreeMap<>(EntryTable.BY_SIZE);

        PartitionGroup group(PopCount pop, EntryTable root) {

            if(root.isEmpty()) {
                return PartitionGroup.EMPTY;
            }

            final Map<GroupFilter, Integer> groups = new TreeMap<>();

            for (PopCount clop : PopCount.TABLE.subList(0, 25)) {
                if(clop.le(pop))
                    for(int rad=0; rad<81; ++rad) {
                        GroupFilter filter = GroupFilter.of(clop.index, rad);
                        EntryTable table = root.filter(filter);
                        Integer key = allocateKey(table);
                        if(key!=null)
                            groups.put(filter, key);
                    }
            }

            return new PartitionGroup(root, groups);
        }

        Partition partition(PopCount pop) {
            tables.clear();
            tmap.clear();

            EntryTable root = RingEntry.MINIMIZED.filter(pop.eq);
            if(root.isEmpty())
                return Partition.EMPTY;

            PartitionGroup groups[] = new PartitionGroup[128];
            List<PartitionGroup> gset = new LinkedList<>();

            /**
             * The groups[128] contain only a few different entries.
             * PGroup.Set maps some index to an unique index with an equivalent entry which may be copied.
             */

            final Set<PGroup> pset = PGroup.groups(root);

            // populate all partitions
            for (int msk = 0; msk < 128; ++msk) {

                PartitionGroup group = groups[msk];
                if(group!=null) // already done
                    continue;

                // try get an entry which may have been calculated before.
                int part = PGroup.pindex(pset, msk);

                group = groups[part];
                if (group == null) {
                    // generate a new partition
                    group = group(pop, root.filter(PGroup.filter(msk)));
                    groups[part] = group;
                    if(!group.isEmpty())
                        gset.add(group);
                }

                // populate entry
                if (part != msk)
                    groups[msk] = group;
            }

            return new Partition(tables, Arrays.asList(groups), gset);
        }

        private Integer allocateKey(EntryTable table) {
            if(table.isEmpty())
                return null;

            if(table.size()==1)
                return -2-table.get(0).index;

            Integer key = tmap.get(table);
            if(key==null) {
                key = tables.size();
                tables.add(table);
                tmap.put(table, key);
            }

            return key;
        }
    }
}
