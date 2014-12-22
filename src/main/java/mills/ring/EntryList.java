package mills.ring;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/22/14
 * Time: 3:43 PM
 */

import mills.util.AbstractRandomList;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Class EntryList manages a mutable list of RingEntries.
 * Elements may be replaced but the size can not be changed.
 * This allows reordering of elements.
 */
public class EntryList extends AbstractRandomList<RingEntry> {

    private final short ringIndex[];

    public EntryList(short[] ringIndex) {
        this.ringIndex = ringIndex;
    }

    @Override
    public int size() {
        return ringIndex.length;
    }

    @Override
    public RingEntry get(int i) {
        final short index = this.ringIndex[i];
        return RingEntry.of(index);
    }

    @Override
    public RingEntry set(int index, RingEntry element) {
        RingEntry previous = get(index);
        ringIndex[index] = element.index;
        return previous;
    }

    public static void sort(short[] ringIndex, @Nullable Comparator<? super RingEntry> comparator) {
        // todo: use simple quicksort on small arrays
        new EntryList(ringIndex).sort(comparator);
    }

    static short[] getIndex(List<? extends RingEntry> list, @Nullable Comparator<? super RingEntry> cmp) {

        if(cmp==null)
            cmp = RingEntry.COMPARATOR;

        final int size = list.size();

        short index[] = new short[size];

        RingEntry e = list.get(0);
        index [0] = e.index;
        boolean ordered = true;

        for(int i=1; i<size; i++) {
            RingEntry f = list.get(i);
            index[i] = f.index;
            ordered &= cmp.compare(e,f)<0;e = f;
        }

        if(!ordered)
            EntryList.sort(index, cmp);

        return index;
    }
}
