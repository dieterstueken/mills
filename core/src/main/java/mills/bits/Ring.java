package mills.bits;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.09.2010
 * Time: 20:13:36
 */
public enum Ring {

    OUTER(3), INNER(1), MIDDLE(2);

    public final int radius;

    Ring(int radius) {
        this.radius = radius;
    }

    public int getMask(int stones) {
        return stones >>> 8 * radius;
    }

    public int getStones(int mask) {
        return mask << 8 * radius;
    }

    public long seek(final short ring) {
        return ring << 16*ordinal();
    }

    public static final List<Ring> RINGS = List.of(values());

    public static Ring of(int i) {
        return RINGS.get(i);
    }
}
