package mills.ring;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  10.09.2019 12:02
 * modified by: $
 * modified on: $
 */
public interface Entries {

    /**
     * Immutable tables.
     */
    RingTable TABLE = new RingTable();
    MinEntry EMPTY = TABLE.getFirst();
    DirectTable RADIALS = TABLE.headList(81);
    EntryTable MINIMIZED = TABLE.filter(RingEntry::isMin);

    static RingEntry entry(int index) {
        return TABLE.get(index);
    }

    static RingEntry empty() {
        return EMPTY;
    }

    static short validateIndex(int index) {
        if(index<0 || index>=MAX_INDEX)
            throw new IndexOutOfBoundsException("invalid index: " + index);

        return (short) index;
    }

    Predicate<RingEntry> ALL  = e -> true;
    Predicate<RingEntry> NONE = e -> false;

    Comparator<List<RingEntry>> BY_SIZE = (t1, t2) -> {

        if(t1==t2)
            return 0;

        if(t1==null)
            return -1;

        int result = Integer.compare(t1.size(), t2.size());

        for(int i=0; result==0 && i<t1.size(); ++i)
            result = RingEntry.COMPARATOR.compare(t1.get(i), t2.get(i));

        return result;
    };

    Comparator<List<RingEntry>> BY_ORDER = (t1, t2) -> {

        if(t1==t2)
            return 0;

        if(t1==null)
            return -1;

        int size = Math.min(t1.size(), t2.size());

        int result = 0;
        for(int i=0; result==0 && i<size; ++i)
            result = RingEntry.COMPARATOR.compare(t1.get(i), t2.get(i));

        if(result==0)
            result = Integer.compare(t1.size(), t2.size());

        return result;
    };

}
