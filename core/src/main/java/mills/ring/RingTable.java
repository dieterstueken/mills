package mills.ring;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.09.2010
 * Time: 13:37:50
 */

import mills.util.UnitArray;

import java.util.Arrays;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Class RingTable is the complete EntryTable of 6561 RingEntries.
 */
class RingTable extends UnitArray<RingEntry> implements EntryTable {

    protected RingTable(RingEntry[] values) {
        super(values);
        verify();
    }

    boolean inRange(int index) {return index>=0 && index<size();}

    // for the full table there is no need to search any entry.
    public int findIndex(int index) {
        return inRange(index) ? index : -1;
    }

    @Override
    public RingEntry getEntry(int index) {
        return get(index);
    }

    public short ringIndex(int index) {

        assert inRange(index) : "invalid RingTable index: " + index;

        return (short) index;
    }

    @Override
    public EntryTable subList(int fromIndex, int toIndex) {
        int size = checkRange(fromIndex, toIndex);

        if(size==0)
            return EMPTY;

        if(size==1)
            return Entries.of(fromIndex).singleton;

        if(fromIndex==0)
            return subSet(toIndex);
        else {
            return new SubTable(this, fromIndex, size);
        }
    }

    @Override
    public RingTable subSet(int size) {
        if(size==size())
            return this;

        if(size<0 || size>=size())
            throw new IndexOutOfBoundsException("size: " + size);

        return new RingTable(values) {
            @Override
            public int size() {
                return size;
            }
        };
    }

    static RingTable create() {
        RingEntry[] table = new RingEntry[MAX_INDEX];
        Arrays.parallelSetAll(table, index -> new RingEntry((short) index));
        return new RingTable(table);
    }
}
