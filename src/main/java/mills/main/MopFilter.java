package mills.main;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.bits.Sector;
import mills.index2.PartitionTables;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.06.2014 14:44
 * modified by: $Author$
 * modified on: $Date$
 */
public class MopFilter implements Runnable {

    final List<Predicate<RingEntry>> filters = new ArrayList<>(81*25);

    MopFilter() {

        for(int nb=0; nb<5;nb++) {
            for(int nw=0; nw<5; nw++) {
                PopCount closed = PopCount.of(nb, nw);
                for(int i=0; i<81; i++) {
                    RingEntry radials = RingEntry.of(81*i);
                    filters.add(filter(closed, radials));
                }
            }
        }

    }

    static Predicate<RingEntry> filter(final PopCount closed, final RingEntry radials) {

        return new Predicate<RingEntry>() {

            public boolean test(final RingEntry entry) {
                PopCount count = closed.sub(entry.closed());

                if(count!=null)
                    for (Sector s : Sector.EDGES) {
                        Player p1 = radials.getPlayer(s);
                        if(p1!=Player.None) {
                            Player p2 = entry.getPlayer(s);
                            if (p1 == p2) {
                                count = count.sub(p1.pop);
                                if (count == null)
                                    break;
                            }
                        }
                    }

                return PopCount.EMPTY.equals(count);
            }

            public String toString() {
                return radials.pattern(new StringBuilder()).insert(0, closed).toString();
            }
        };
    }

    boolean any(RingEntry e) {
        for(Predicate<RingEntry> f:filters) {
            if(f.test(e))
                return true;
        }

        return false;
    }

    @Override
    public void run() {

        /*for(RingEntry e: RingEntry.MINIMIZED) {
            if(!any(e)) {
                System.out.format("none: %s\n", e);

                final Predicate<RingEntry> f = filters.get(81 * 5);
                f.test(e);
            }
        }*/

        final Set<EntryTable> tables = new TreeSet<>();

        for(List<EntryTable> p:PartitionTables.INSTANCE) {
            for(EntryTable t:p) {
                for(Predicate<RingEntry> f:filters) {
                    EntryTable result = t.filter(f);
                    tables.add(result);
                }
            }
        }

        System.out.format("%d tables\n", tables.size());

        int l=0;
        int n=0;

        for(EntryTable t:tables) {
            int len = t.size();
            if(len!=l) {
                System.out.format("%2d %4d\n", l, n);
                l = len;
                n=0;
            } else
                ++n;
        }

        System.out.format("%2d %4d\n", l, n);
    }

    public static void main(String ... args) {
        new MopFilter().run();
    }
}
