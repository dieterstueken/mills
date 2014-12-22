package mills.ring;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
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
        assert isOrdered(this, comparator());
    }

    public static EntryArray of(short[] ringIndex, @Nullable Comparator<? super RingEntry> comparator) {

        return comparator==null ? new EntryArray(ringIndex) :
            new EntryArray(ringIndex) {

                @Override
                public Comparator<? super RingEntry> comparator() {
                    return comparator;
                }
            };
    }

    public static EntryArray of(List<? extends RingEntry> list, @Nullable Comparator<? super RingEntry> comparator) {

        short table[] = EntryList.getIndex(list, comparator);

        return EntryArray.of(table, comparator);
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
