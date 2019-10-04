package mills.ring;

import mills.util.AbstractListSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.10.19
 * Time: 01:01
 */
abstract public class EntryList extends AbstractListSet<RingEntry> implements EntryTable {

    public RingEntry getEntry(int index) {
        return Entries.of(index);
    }

    @Override
    public RingEntry get(int i) {
        final int index = ringIndex(i);
        return getEntry(index);
    }

    public short ringIndex(int i) {
        return get(i).index;
    }

    @Override
    public EntryTable subList(int fromIndex, int toIndex) {
        int size = checkRange(fromIndex, toIndex);

        if(size==0)
            return EntryTable.EMPTY;

        if(size==1)
            return get(fromIndex).singleton;

        if(size==this.size())
            return this;

        return new SubTable(this, fromIndex, size);
    }
}
