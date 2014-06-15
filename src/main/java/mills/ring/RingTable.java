package mills.ring;

import com.google.common.collect.Iterables;
import mills.bits.BW;
import mills.bits.Pattern;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.09.2010
 * Time: 13:37:50
 */

/**
 * Class RingTable is the complete EntryTable of 6561 RingEntries.
 */
public class RingTable extends EntryTable {

    private final RingEntry entries[];

    RingTable(final List<RingEntry> entries) {
        this.entries = Iterables.toArray(entries, RingEntry.class);
    }

    @Override
    public int size() {
        return entries.length;
    }

    @Override
    public RingEntry get(int index) {
        return entries[index];
    }

    // for the full table there is no need to search any entry.
    public int indexOf(short index) {
        return index>=0 && index<size() ? index : -1;
    }

    public short ringIndex(int index) {

        if(index<0 && index>=size())
            throw new IndexOutOfBoundsException("invalid RingTable index: " + index);

        return (short) index;
    }

    public static void main(String ... args) {

        for(RingEntry e:RingEntry.TABLE) {

            Pattern b = e.b;
            Pattern w = e.w;

            int index = BW.index(b, w);

            RingEntry f = RingEntry.of(index);

            if(e!=f)
                System.out.format("%d %d %d\n", index, e.index, f.index);

        }
    }
}
