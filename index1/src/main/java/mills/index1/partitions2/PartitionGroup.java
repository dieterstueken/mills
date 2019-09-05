package mills.index1.partitions2;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.function.ToIntFunction;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  21.07.2014 10:54
 * modified by: $Author$
 * modified on: $Date$
 */
public class PartitionGroup extends AbstractRandomList<RadialGroup> {

    public final EntryTable root;

    private final RadialGroup groups[];  // 25

    public PartitionGroup(EntryTable root, RadialGroup groups[]) {
        this.root = root;
        this.groups = groups;
        assert groups.length == 25;
    }

    public static final PartitionGroup EMPTY = new PartitionGroup(EntryTable.EMPTY, new RadialGroup[25]) {
        @Override
        public int getKey(PopCount clop, int radials) {
            return 0;
        }

        @Override
        public String toString() {
            return "empty";
        }
    };

    @Override
    public int size() {
        return 25;
    }

    @Override
    public RadialGroup get(int radials) {
        return groups[radials];
    }

    public int getKey(PopCount clop, int radials) {
        RadialGroup group = groups[clop.index];
        return group==null ? 0 : group.getKey(radials);
    }

    int count() {
        int count = 0;
        for (RadialGroup group : groups) {
            if(group!=null)
                ++count;
        }

        return count;
    }

    public boolean isEmpty() {
        return root.isEmpty();
    }

    static PartitionGroup build(PopCount pop, EntryTable root, ToIntFunction<EntryTable> index) {

        if(root.isEmpty()) {
            return PartitionGroup.EMPTY;
        }

        RadialGroup groups[] = new RadialGroup[25];

        PopCount.TABLE.subList(0, 25).stream().filter(clop -> clop.le(pop)).forEach(clop -> {
            groups[clop.index] = RadialGroup.build(root, GroupFilter.filters(clop.index), index);
        });

        return new PartitionGroup(root, groups);
    }
}
