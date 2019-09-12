package mills.bits;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  22.06.2016 11:46
 * modified by: $Author$
 * modified on: $Date$
 */

import mills.ring.Entry;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class RClop is a combination of radial positions (possible mills)
 * and a PopCount of closed mills.
 *
 * RClop accepts a RingEntry if its clop count plus the count of closing radials matches this given popCount.
 */
public class RClop implements Comparable<RClop> {

    public final PopCount clop;

    public final RingEntry rad;

    public static int index(RingEntry rad, PopCount clop) {
        assert clop.max()<=4;
        return rad.radix() + 81 * clop.index;
    }

    @Override
    public String toString() {
        return String.format("%02d:%s", rad.index, clop.toString());
    }

    private RClop(int index) {
        this.clop = PopCount.CLOSED.get(index/81);
        this.rad = Entry.RADIALS.get(index%81);
    }

    public int index() {
        return index(rad, clop);
    }

    // add radials and clop
    public RClop add(RClop other) {
        return of(rad.and(other.rad), clop.add(other.clop));
    }

    // resulting closed count including radials
    public PopCount clop() {
        return clop.add(rad.pop);
    }

    public static final int SIZE = Entry.RADIALS.size()*PopCount.CLOSED.size();
    public static final List<RClop> TABLE = AbstractRandomList.generate(SIZE, RClop::new);

    public static RClop of(RingEntry entry) {
        return of(entry.radials(), entry.clop());
    }

    public static RClop of(RingEntry rad, PopCount clop) {
        return TABLE.get(index(rad, clop));
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
        return index();
    }

    @Override
    public int compareTo(RClop o) {
        return Integer.compare(index(), o.index());
    }

    // singletons: no equals necessary.

    public static void main(String ... args) {
        EntryTables pool = new EntryTables();

        int l = 0;

        for (PopCount pop : PopCount.TABLE) {
            EntryTable pt = Entry.MINIMIZED.filter(pop.eq);
            if(pt.isEmpty())
                continue;

            int n=0;

            for (PopCount clop : PopCount.CLOSED) {

                int m=0;
                Map<RingEntry, EntryTable> tables = new TreeMap<>();

                int nr = 0;
                for (RingEntry rad : Entry.RADIALS) {
                    RClop rcl = RClop.of(rad, clop);
                    EntryTable rt = pt.filter(rcl::matches);

                    if(rt.isEmpty())
                        continue;

                    tables.put(rad, rt);
                    pool.key(rt);

                    if(rt.size()>m) {
                        m = rt.size();
                    }
                    ++nr;
                }

                if(!tables.isEmpty()) {
                    System.out.format("%s/%s %d %d %d\n", pop, clop, nr, tables.size(), m);
                    ++n;
                    ++l;
                }
            }

            if(n>0)
                System.out.println();
        }

        System.out.format("total : %d %d\n", l, pool.count());

    }
}
