package mills.ring;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 22.10.11
 * Time: 20:14
 */
class IndexTable extends EntryTable {

    private final short ringIndex[];

    IndexTable(short[] ringIndex) {
        this.ringIndex = ringIndex;
    }

    static IndexTable of(short[] ringIndex) {
        return new IndexTable(ringIndex);
    }

    @Override
    public int findIndex(short ringIndex) {
        return Arrays.binarySearch(this.ringIndex, ringIndex);
    }

    public short ringIndex(int i) {
        return ringIndex[i];
    }

    @Override
    public RingEntry get(int i) {
        final short index = this.ringIndex[i];
        return RingEntry.of(index);
    }

    @Override
    public int size() {
        return ringIndex.length;
    }

    @Override
    public EntryTable subList(int fromIndex, int toIndex) {

        if(fromIndex==0 && toIndex==size())
            return this;

        return EntryTable.of(ringIndex, fromIndex, toIndex);
    }
}
