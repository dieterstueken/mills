package mills.bits;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  22.06.2016 11:46
 * modified by: $Author$
 * modified on: $Date$
 */

import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.Indexed;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class RClop is a combination of radial positions (possible mills)
 * and a PopCount of closed mills.
 *
 * RClop accepts a RingEntry if its clop count plus the count of closing radials matches this given popCount.
 */
public class RClop implements Indexed {

    public final PopCount clop;

    public final RingEntry rad;

    public static int getIndex(RingEntry rad, PopCount clop) {
        assert clop.max()<=4;
        return rad.radix() + 81 * clop.index;
    }

    @Override
    public String toString() {
        return String.format("%02d/%s", rad.index, clop.toString());
    }

    private RClop(int index) {
        this.clop = PopCount.CLOSED.get(index/81);
        this.rad = Entries.RADIALS.get(index%81);
    }

    public int getIndex() {
        return getIndex(rad, clop);
    }

    // add radials and clop
    public RClop add(RClop other) {
        return of(rad.and(other.rad), clop.add(other.clop));
    }

    // resulting closed count including radials
    public PopCount clop() {
        return clop.add(rad.pop);
    }

    public static final int SIZE = Entries.RADIALS.size()*PopCount.CLOSED.size();
    public static final List<RClop> TABLE = AbstractRandomList.generate(SIZE, RClop::new);

    public static RClop of(RingEntry entry) {
        return of(entry.radials(), entry.clop());
    }

    public static RClop of(RingEntry rad, PopCount clop) {
        return TABLE.get(getIndex(rad, clop));
    }

    /**
     * A matching entry provides additional radials and closed which must match this.closed.
     * @param entry to analyze.
     * @return if the entry matches the required clop.
     */
    public boolean matches(RingEntry entry) {
        return entry.and(rad).clop().add(entry.clop()).equals(clop);
    }

    public int hashCode() {
        return getIndex();
    }

    // singletons: no equals necessary.

    static boolean matches(RingEntry e, RingEntry rad, PopCount clop) {
        rad = rad.and(e);

        // verify sum # of stones to build radial mills
        PopCount xpop = PopCount.of(9,9);
        xpop = xpop.sub(rad.pop).sub(rad.pop).sub(rad.pop);

        if(xpop==null)
            return false;

        // plus actual stones.
        xpop = xpop.sub(e.pop);
        if(xpop==null)
            return false;

        return rad.pop().add(e.clop()).equals(clop);
    }

    public static void main(String ... args) {
        EntryTables pool = new EntryTables();

        for (PopCount pop : PopCount.TABLE) {
            EntryTable pt = Entries.MINIMIZED.filter(pop.eq);
            if(pt.isEmpty())
                continue;

            for (PopCount clop : PopCount.CLOSED) {
                Set<EntryTable> tset = new TreeSet<>(Entries.BY_SIZE);

                for (RingEntry rad : Entries.RADIALS) {
                    EntryTable rt = pt.filter(e->matches(e, rad, clop));
                    if(rt.isEmpty())
                        continue;

                    tset.add(rt);
                    pool.key(rt);
                }

                if(tset.isEmpty())
                    continue;

                System.out.format("%s %s %d\n", pop, clop, tset.size());
            }
            System.out.println();
        }

        System.out.format("total : %d\n\n", pool.count());

        pool.stat(System.out);

        System.out.println("radixed:");

        int[] rcount = new int[81];

        for (RingEntry min : Entries.MINIMIZED) {
            int ir = min.radix();
            ++rcount[ir];
        }

        for(int i=0; i<81; ++i) {
            System.out.format("%s  %d\n", Entries.RADIALS.get(i).toString().substring(7, 11), rcount[i]);
        }
    }
}
