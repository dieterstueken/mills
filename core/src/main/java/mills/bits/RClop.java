package mills.bits;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  22.06.2016 11:46
 * modified by: $Author$
 * modified on: $Date$
 */

import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Class RClop is a combination of radial positions (possible mills)
 * and a PopCount of closed mills.
 */
public class RClop {

    public final PopCount clop;

    public final RingEntry rad;

    public static int index(RingEntry rad, PopCount clop) {
        assert rad.radials().equals(rad);
        assert clop.max()<=4;
        return rad.radix() + 81 * clop.index;
    }

    private RClop(int index) {
        this.clop = PopCount.CLOSED.get(index/81);
        this.rad = RingEntry.RADIALS.get(index%81);
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

    public static final List<RClop> TABLE = AbstractRandomList.generate(81*24, RClop::new);

    public static RClop of(RingEntry rad, PopCount clop) {
        return TABLE.get(index(rad, clop));
    }

    public static RClop of(RingEntry entry) {
        return of(entry.radials(), entry.clop());
    }

    public int hashCode() {
        return index();
    }

    // singletons: no equals necessary.
}
