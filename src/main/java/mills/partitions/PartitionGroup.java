package mills.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;

import java.util.Collections;
import java.util.Map;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  21.07.2014 10:54
 * modified by: $Author$
 * modified on: $Date$
 */
public class PartitionGroup {

    final EntryTable root;

    final Map<GroupFilter, Integer> groups;

    public PartitionGroup(EntryTable root, Map<GroupFilter, Integer> groups) {
        this.root = root;
        this.groups = groups;
    }

    public int getKey(PopCount clop, int radials) {
        GroupFilter groupFilter = GroupFilter.of(clop, radials);
        Integer key = groups.get(groupFilter);

        return key==null ? -1 : key;
    }

    public static final PartitionGroup EMPTY = new PartitionGroup(EntryTable.EMPTY, Collections.emptyMap()) {
        @Override
        public int getKey(PopCount clop, int radials) {
            return -1;
        }
    };
}
