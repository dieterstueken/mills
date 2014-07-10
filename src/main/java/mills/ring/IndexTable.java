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
        assert isOrdered(ringIndex);
        this.ringIndex = ringIndex;
    }

    static IndexTable of(short[] ringIndex) {
        return new IndexTable(ringIndex);
    }

    @Override
    public int findIndex(int ringIndex) {
        return Arrays.binarySearch(this.ringIndex, (short) ringIndex);
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

        if(fromIndex>=toIndex)
            return EMPTY;

        if(toIndex==fromIndex+1)
            return get(fromIndex).singleton;

        return EntryTable.of(ringIndex, fromIndex, toIndex);
    }

    public static boolean isOrdered(short[] table) {
        if(table!=null && table.length>1) {
            short k = table[0];
            for (int i = 1; i < table.length; ++i) {
                short l = table[i];
                if(l<=k)
                    return false;
                k = l;
            }
        }

        return  true;
    }

}
