package mills.index1.partitions2;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.bits.Sector;
import mills.ring.Entry;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  21.07.2014 11:19
 * modified by: $Author$
 * modified on: $Date$
 */
public class Radials {

    final RingEntry radials;

    private Radials(int index) {
        radials = Entry.RADIALS.get(index);
    }

    @Override
    public int hashCode() {
        return radials.index();
    }

    public int index() {
            return radials.index();
        }

    @Override
    public String toString() {
        return radials.toString().substring(7, 11);
    }

    boolean matches(RingEntry entry) {
        return entry.radials() == this.radials;
    }

    public PopCount clop(RingEntry ringEntry, Sector sector) {
        Player player = ringEntry.player(sector);
        return player!=Player.None && player==radials.player(sector) ? player.pop : PopCount.EMPTY;
    }

    public PopCount clop(RingEntry e) {

        PopCount clop = clop(e, Sector.N);
        clop = clop.add(clop(e, Sector.E));
        clop = clop.add(clop(e, Sector.S));
        clop = clop.add(clop(e, Sector.W));

        return clop;
    }

    static final List<Radials> RADIALS = AbstractRandomList.generate(81, Radials::new);

    public static Radials of(int index) {
        return RADIALS.get(index);
    }

    public static int index(RingEntry a, RingEntry b) {

        int radials = 0;

        // reverse iteration: high sectors first
        for(int i=3; i>=0; --i) {
            Sector s = Sector.EDGES.get(i);
            Player player = a.player(s);

            radials *= 3;
            if(player!=Player.None && player==b.player(s))
                radials += player.ordinal();
        }

        return radials;
    }

    public static void main(String ... args) {

        for (Radials radial : RADIALS) {
            EntryTable rt = Entry.MINIMIZED.filter(radial::matches);
            System.out.format("%2d %s %d\n", radial.index(), radial.toString(), rt.size());
        }
    }
}
