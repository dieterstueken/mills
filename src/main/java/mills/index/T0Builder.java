package mills.index;

import mills.bits.PopCount;
import mills.index.partitions.LePopTable;
import mills.index.partitions.PartitionTables;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.IndexTable;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  01.08.12 16:34
 * modified by: $Author$
 * modified on: $Date$
 */

/**
 * This Builder is a mutable R1Table and is not thread safe!
 */
class T0Builder extends R1Table {

    final LePopTable lePopTable;

    final int index[] = new int[MAX_INDEX];

    final short t0[] = new short[MAX_INDEX];

    T0Builder(PartitionTables partitions, LePopTable lePopTable) {
        super(partitions, new short[MAX_INDEX]);
        this.lePopTable = lePopTable;
    }

    // build for an given index e2 and some remaining pop count
    public R0Table build(final PopCount pop, final RingEntry e2) {

        int size = 0;
        int count = 0;

        final EntryTable let = lePopTable.get(pop);
        for (final RingEntry e0 : let) {

            // i2<=t0
            if (e0.index > e2.index)
                break;

            final PopCount pop1 = pop.sub(e0.pop);

            // no stones remain for ring #0
            if (pop1 == null)
                continue;

            // lookup entry popmsk
            short msk = e2.mlt20s(e0);

            final EntryTable entries = partitions.get(pop1).get(msk);

            // ignore all empty partitions
            if (entries.isEmpty())
                continue;

            // shift pop count into upper bits
            msk = indexOf(pop1, msk);

            t0[size] = e0.index;
            popmsk[size] = msk;
            index[size] = count;

            ++size;
            count += entries.size();
        }

        IndexTable it = IndexTable.of(index, size);
        EntryTable r0 = EntryTable.of(t0, size);
        R1Table table = copyOf(size);

        return R0Table.of(it, r0, table);
    }
}
