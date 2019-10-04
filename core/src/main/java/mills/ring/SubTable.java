package mills.ring;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  10.07.2014 11:56
 * modified by: $Author$
 * modified on: $Date$
 */
public class SubTable extends EntryList {

    final EntryTable parent;
    final int offset;
    final int size;

    SubTable(EntryTable parent, int offset, int size) {
        this.parent = parent;
        this.offset = offset;
        this.size = size;

        assert offset >= 0;
        assert size > 1;    // should be empty or a singleton instead
    }

    @Override
    public RingEntry getEntry(int index) {
        return parent.getEntry(index);
    }

    @Override
    public int findIndex(int ringIndex) {
        int index = parent.findIndex(ringIndex);

        if(index<offset) {
            index+=offset;
            if(index>=0) // was between [0,offset[
                return -1;

            // was negative, limit negative size.
            int limit = -(size+1);
            return Math.max(index, limit);
        }

        // index >= offset
        index -= offset;
        return index<size ? index : -(size+1);
    }

    @Override
    public EntryTable subList(int fromIndex, int toIndex) {

        // Prevent from extending the range beyond given bounds.
        checkRange(fromIndex, toIndex);

        return parent.subList(fromIndex + offset, toIndex + offset);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public RingEntry get(int index) {
        return parent.get(index+offset);
    }
}
