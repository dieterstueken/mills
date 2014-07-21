package mills.partitions;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.bits.Sector;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.function.Function;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  21.07.2014 11:19
 * modified by: $Author$
 * modified on: $Date$
 */
public class Radials implements Function<RingEntry, PopCount> {

    final RingEntry radials;

    private Radials(int index) {
        radials = RingEntry.of(81*index);
    }

    public int hashCode() {
        return radials.index();
    }

    public String toString() {
        return radials.toString().substring(12, 16);
    }

    PopCount clop(RingEntry ringEntry, Sector sector) {
        Player p1 = ringEntry.player(sector);
        return p1==Player.None || p1==radials.player(sector) ? p1.pop : PopCount.EMPTY;
    }

    @Override
    public PopCount apply(RingEntry e) {
        PopCount clop = PopCount.EMPTY;

        clop = clop.add(clop(e, Sector.N));
        clop = clop.add(clop(e, Sector.E));
        clop = clop.add(clop(e, Sector.S));
        clop = clop.add(clop(e, Sector.W));

        return clop;
    }

    static final List<Radials> RADIALS = AbstractRandomList.generate(81, Radials::new);

    public static Radials of(int index) {
        return RADIALS.get(index);
    }
}
