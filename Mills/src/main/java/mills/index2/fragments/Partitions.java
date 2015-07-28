package mills.index2.fragments;

import mills.bits.PopCount;

import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 20:12
 * modified by: $Author$
 * modified on: $Date$
 */
public class Partitions {

    final List<PartitionTable> partitions;

    public Partitions(List<PartitionTable> partitions) {
        this.partitions = partitions;
    }

    public PartitionTable get(PopCount pop) {
        return partitions.get(pop.index);
    }
}
