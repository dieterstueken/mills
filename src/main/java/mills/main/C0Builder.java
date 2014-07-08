package mills.main;

import com.google.common.collect.ImmutableList;
import mills.bits.PopCount;
import mills.index.*;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.07.2014 10:14
 * modified by: $Author$
 * modified on: $Date$
 */
public class C0Builder {

    final Partitions partitions = Partitions.open();
    final IndexList indexes = IndexList.create();

    public static void main(String... args) {
        C0Builder main = new C0Builder();
        main.run();
    }

    void run() {

        for (PopCount pop : PopCount.TABLE) {
            System.out.println(pop);
            verify(pop);
        }
    }

    private void verify(final PopCount pop) {

        R2Index posIndex = indexes.get(pop);
        int range = posIndex.size();
        int n20 = posIndex.entries().size();

        System.out.format("%s %10d, %4d\n", pop, range, n20);
        posIndex = null;

        final R2Table r2t = buildR2(pop);
        range = r2t.size();
        n20 = r2t.entries().size();

        System.out.format("%s %10d, %4d\n", pop, range, n20);
    }

     R2Table buildR2(PopCount pop) {

         final EntryTable let = partitions.lePopTable.get(pop);

         List<RingEntry> t2 = new ArrayList<>(let.size());
         List<R0Table> t0 = new ArrayList<>(let.size());

         for(final RingEntry e2:let) {

             final PopCount pop2 = pop.sub(e2.pop);

             // no stones remain
             if (pop2 == null)
                 continue;

             R0Table r0 = buildR0(pop2, e2);
             if(r0.entries().isEmpty())
                 continue;

             t2.add(e2);
             t0.add(r0);
         }

         return R2Table.of(EntryTable.of(t2), ImmutableList.copyOf(t0));
    }

    R0Table buildR0(final PopCount pop, final RingEntry e2) {

        final EntryTable let = partitions.lePopTable.get(pop);

        List<RingEntry> t0 = new ArrayList<>(let.size());
        List<EntryTable> t1 = new ArrayList<>(let.size());

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

            final EntryTable entries = partitions.partitions.get(pop1).get(msk);
            // ignore all empty partitions
            if (entries.isEmpty())
                continue;

            t0.add(e0);
            t1.add(entries);
        }

        return t0.isEmpty() ? R0Table.EMPTY : R0Table.of(EntryTable.of(t0), ImmutableList.copyOf(t1));
    }
}
