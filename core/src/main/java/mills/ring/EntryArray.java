package mills.ring;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 22.10.11
 * Time: 20:14
 */
class EntryArray extends EntryList {

    final short[] indices;

    protected EntryArray(short[] indices) {
        this.indices = indices;
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
        return getEntry(index);
    }

    @Override
    public int size() {
        return indices.length;
    }

    public static EntryArray of(short[] indices) {
        assert isOrdered(indices) : "index mismatch";
        return new EntryArray(indices);
    }

    @Override
    protected EntryArray verify() {
        assert isOrdered(indices) : "index mismatch";
        return this;
    }

    static boolean isOrdered(short[] values) {

        if(values.length<2)
            return true;

        short t0 = values[0];
        for (int i = 1; i < values.length; ++i) {
            short t1 = values[i];
            if(Short.compare(t0, t1)>=0)
                return false;
            t0 = t1;
        }

        return  true;
    }
}
