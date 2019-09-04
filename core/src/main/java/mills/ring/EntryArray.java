package mills.ring;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 22.10.11
 * Time: 20:14
 */
class EntryArray extends EntryTable {

    protected final short indices[];

    protected EntryArray(short[] indices) {
        this.indices = indices;
        this.modCount = Arrays.hashCode(indices);
        assert isOrdered(indices);
    }

    @Override
    public int hashCode() {
        return modCount;
    }

    static EntryTable of(short[] ringIndex) {
        return new EntryArray(ringIndex);
    }

    @Override
    protected EntryTable partition(int fromIndex, int range) {
        // called internally if not empty nor a singleton
        return of(Arrays.copyOfRange(indices, fromIndex, fromIndex+range));
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
        final short index = this.indices[i];
        return RingEntry.of(index);
    }

    @Override
    public int size() {
        return indices.length;
    }
}
