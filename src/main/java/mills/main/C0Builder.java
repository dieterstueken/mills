package mills.main;

import com.google.common.collect.ImmutableList;
import mills.bits.PopCount;
import mills.index.*;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.ring.RingTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

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
        new C0Builder().run();
    }

    final Set<EntryTable> tables = new ConcurrentSkipListSet<>(EntryTable.BY_ORDER);

    void run() {
        PopCount.TABLE.forEach(this::count);

        tables.forEach(
            t->{
                t.forEach(e->System.out.format("%d ", e.index()));
                System.out.println();
            }
        );
    }

    private void verify(final PopCount pop) {
        System.out.println(pop);

        R2Index posIndex = indexes.get(pop);
        int range = posIndex.range();
        int n20 = posIndex.values().size();

        System.out.format("%s %10d, %4d\n", pop, range, n20);
        posIndex = null;

        final R2Table r2t = buildR2(pop);
        range = r2t.range();
        n20 = r2t.values().size();

        System.out.format("%s %10d, %4d\n", pop, range, n20);
    }

    private void count(final PopCount pop) {

        R2Table index = buildR2(pop);

        int range = index.range();
        int n20 = index.values().size();

        System.out.format("%s %10d, %4d\n", pop, range, n20);

        ClopIndex ci = new ClopIndex.Builder(index){
            @Override
            public EntryTable table(List<RingEntry> list) {
                EntryTable table = EntryTable.of(list);
                //tables.add(table);
                return table;
            }
        }.compute();

        for(int i=0; i<25; ++i) {
            index = ci.get(i);

            if(index.isEmpty())
                continue;

            PopCount clop = PopCount.TABLE.get(i);
            range = index.range();
            n20 = index.values().size();

            System.out.format("\t%s %10d, %4d\n", clop, range, n20);
        }

        System.out.format("size: %d\n", tables.size());
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
             if(r0.values().isEmpty())
                 continue;

             t2.add(e2);
             t0.add(r0);
         }

         return R2Table.of(pop, EntryTable.of(t2), ImmutableList.copyOf(t0));
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
