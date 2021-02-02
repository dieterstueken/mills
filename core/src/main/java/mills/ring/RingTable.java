package mills.ring;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.09.2010
 * Time: 13:37:50
 */

import java.util.Arrays;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Class RingTable is the complete EntryTable of 6561 RingEntries.
 */
class RingTable extends AbstractEntryTable implements IndexedEntryTable {

    private final RingEntry[] entries = RingEntry.table();

    private final int hashCode;

    RingTable() {
        assert entries.length == MAX_INDEX;
        this.hashCode = Arrays.hashCode(entries);
    }

    @Override
    public int size() {
        return MAX_INDEX;
    }

    @Override
    public int getIndex() {
        return MAX_INDEX;
    }

    @Override
    public RingEntry get(int index) {
        return entries[index];
    }

    boolean inRange(int index) {return index>=0 && index<MAX_INDEX;}

    // for the full table there is no need to search any entry.
    public int findIndex(int index) {
        return inRange(index) ? index : -1;
    }

    public short ringIndex(int index) {

        assert inRange(index) : "invalid RingTable index: " + index;

        return (short) index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof RingTable) {
            RingTable that = (RingTable) o;
            return Arrays.equals(entries, that.entries);
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
