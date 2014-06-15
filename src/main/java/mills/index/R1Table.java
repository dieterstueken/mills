package mills.index;

import mills.bits.PopCount;
import mills.index.partitions.PartitionTables;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.Arrays;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  01.08.12 16:21
 * modified by: $Author$
 * modified on: $Date$
 */
class R1Table extends AbstractRandomList<EntryTable> {

    final PartitionTables partitions;

    protected final short popmsk[];

    protected R1Table(PartitionTables partitions, short[] popmsk) {
        this.partitions = partitions;
        this.popmsk = popmsk;
    }

    @Override
    public int size() {
        return popmsk.length;
    }

    @Override
    public EntryTable get(int index) {

        final int pm = popmsk[index];

        final int msk = pm & 127;
        final int pop = pm / 128;

        return partitions.get(pop).get(msk);
    }

    public static short indexOf(PopCount pop, short msk) {
        assert msk<128 && msk>=0 : "invalid popmsk";
        msk += 128 * pop.index();
        return msk;
    }

    // create sub table
    public R1Table copyOf(int size) {
        final short[] table = Arrays.copyOf(this.popmsk, size);
        return new R1Table(this.partitions, table);
    }
}
