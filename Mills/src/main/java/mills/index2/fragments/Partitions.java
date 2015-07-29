package mills.index2.fragments;

import mills.bits.PopCount;
import mills.ring.EntryTable;

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

    public Partitions(EntryTable root, List<MaskTable> partitions) {
        super(root);
        this.partitions = partitions;
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
        return partitions;
    }

    public PartitionTable get(PopCount pop) {
        return get(pop.index);
    }
}
