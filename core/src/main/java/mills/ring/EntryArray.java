package mills.ring;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 22.10.11
 * Time: 20:14
 */
abstract public class EntryArray extends AbstractEntryTable {

    final short[] indices;

    final int hashCode;

    protected EntryArray(short[] indices) {
        this.indices = indices;
        this.hashCode = Arrays.hashCode(indices);
    }

    @Override
    public int findIndex(int ringIndex) {
        return Arrays.binarySearch(this.indices, (short) ringIndex);
    }

    public short ringIndex(int i) {
        return indices[i];
    }

    @Override
    public RingEntry get(int i) {
        final int index = ringIndex(i);
        return entry(index);
    }

    abstract protected RingEntry entry(int index);

    @Override
    public int size() {
        return indices.length;
    }

    public static EntryArray of(short[] indices) {
        assert isOrdered(indices) : "index mismatch";
        return new EntryArray(indices) {
            @Override
            protected RingEntry entry(final int index) {
                return RingEntry.of(index);
            }
        };
    }

    static boolean isOrdered(short[] values) {

        if(values.length<2)
            return true;

        short t0 = values[0];
        for (int i = 1; i < values.length; ++i) {
            short t1 = values[i];
            if(t0 >= t1)
                return false;
            t0 = t1;
        }

        return  true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if ((o instanceof EntryArray a)) {

            return Arrays.equals(this.indices, a.indices);
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
