package mills.bits;

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

    public long seek(final short ring) {
        return ring << 16*ordinal();
    }

    private static final Ring rings[] = values();

    public static Ring of(int i) {
        return rings[i];
    }
}
