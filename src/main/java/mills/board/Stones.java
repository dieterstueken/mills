package mills.board;

import mills.bits.Player;
import mills.bits.Ring;
import mills.bits.Sector;
import mills.position.Positions;
import mills.ring.RingEntry;

/*
* version        :  $Revision: $
* created by     :  dst
* date created   :  05.10.2010, 14:42:32
* last mod by    :  $Author: $
* date last mod  :  $Date: $
*
*/
public class Stones {

    long stones = 0;

    public Stones() {}

    public Stones(final RingEntry inner, final RingEntry middle, final RingEntry outer) {
        stones |= Ring.INNER.seek(inner.index);
        stones |= Ring.MIDDLE.seek(middle.index);
        stones |= Ring.OUTER.seek(outer.index);
    }

    public Player getPlayer(int i) {
        final Ring r = Ring.of((i/8)%3);
        final Sector s = Sector.of(i%8);
        return getPlayer(r, s);
    }

    long seek(final Ring r, final Sector s) {
        return r.seek(s.pow3());
    }

    public Player getPlayer(final Ring r, final Sector s) {
        long ip = stones / seek(r, s);
        return Player.of((int)(ip%3));
    }

    public void setPlayer(final Ring r, final Sector s, final Player player) {
        long seek = seek(r, s);
        long ip = stones / seek(r, s);
        ip %= 3;
        ip = player.ordinal() - ip;
        ip *= seek;
        stones += ip;

        // not normalized any more
        stones &= ~Positions.NORMALIZED;
    }
}
