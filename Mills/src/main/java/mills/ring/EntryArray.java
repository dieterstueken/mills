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
    }

    public int hashCode() {
        return modCount;
    }

    static EntryTable of(short[] ringIndex) {
        EntryArray table = new EntryArray(ringIndex) {

            @Override
            public EntryTable subList(int fromIndex, int toIndex) {
                return subsetOf(indices, fromIndex, toIndex);
            }
        };

        assert isOrdered(table, RingEntry.COMPARATOR);
        return table;
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

    static EntryTable subsetOf(short[] indices, int from, int to) {

        if(from<0 || to<from || to>indices.length)
            throw new IllegalArgumentException("invalid index range");

        if(from==to)
            return EMPTY;

        if(from==to-1)
            return SingleEntry.of(indices[from]);

        if(from==0 && to==indices.length)
            return EntryArray.of(indices);

        EntryArray table = new EntryArray(indices) {
            @Override
            public int findIndex(int ringIndex) {
                int index = Arrays.binarySearch(this.indices, from, to, (short) ringIndex);
                index = index<0 ? index+from : index - from;
                return index;
            }

            @Override
            public RingEntry get(int i) {
                rangeCheck(i);
                return super.get(i + from);
            }

            @Override
            public EntryTable subList(int fromIndex, int toIndex) {

                if(fromIndex==0 && toIndex==size())
                    return this;

                    // verify smaller range
                if(fromIndex<0 || toIndex<fromIndex || toIndex-fromIndex>size())
                    throw new IllegalArgumentException("invalid index range");

                // shift to absolute range
                return subsetOf(indices, fromIndex+from, toIndex+from);
            }

            @Override
            public short ringIndex(int i) {
                rangeCheck(i);
                return super.ringIndex(i + from);
            }

            private void rangeCheck(int i) {
                if(i<0 || i>=size()) {
                    String msg = "Index: " + i + ", Size: " + size();
                    throw new IndexOutOfBoundsException(msg);
                }
            }

            @Override
            public int size() {
                return to-from;
            }
        };

        assert isOrdered(table, RingEntry.COMPARATOR);

        return table;
    }
}
