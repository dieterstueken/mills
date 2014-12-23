package mills.ring;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 22.10.11
 * Time: 20:14
 */
class EntryArray extends EntryTable {

    private final short ringIndex[];

    EntryArray(short[] ringIndex) {
        this.ringIndex = ringIndex;
    }

    static EntryArray of(short[] ringIndex) {
        EntryArray table = new EntryArray(ringIndex);
        assert isOrdered(table, RingEntry.COMPARATOR);
        return table;
    }

    public static EntryArray of(List<? extends RingEntry> list) {

        final int size = list.size();

        short index[] = new short[size];

        RingEntry e = list.get(0);
        index [0] = e.index;
        boolean ordered = true;

        for(int i=1; i<size; i++) {
            RingEntry f = list.get(i);
            index[i] = f.index;
            ordered &= e.index>f.index;
        }

        if(!ordered)
            Arrays.sort(index);

        return new EntryArray(index);
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
}
