package mills.index3.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTables;

import java.util.Collection;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 20:12
 * modified by: $Author$
 * modified on: $Date$
 */
public class Partitions extends PartitionTable<MaskTable> {

    final List<MaskTable> partitions;

    final Collection<MaskTable> content;

    public Partitions(List<MaskTable> partitions, Collection<MaskTable> content) {
        this.partitions = partitions;
        this.content = content;
    }

    public static Partitions build() {
        return new Builder(new EntryTables()).partitions();
    }

    @Override
    public MaskTable get(int index) {
        return partitions.get(index);
    }

    @Override
    public int size() {
        return 100;
    }

    @Override
    public Collection<MaskTable> content() {
        return content;
    }

    public MaskTable get(PopCount pop) {
        return get(pop.index);
    }
}
