package mills.ring;

import mills.bits.BW;
import mills.bits.Pattern;

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

    private final RingEntry entries[] = RingEntry.entries();

    @Override
    public int size() {
        return RingEntry.MAX_INDEX;
    }

    @Override
    public RingEntry get(int index) {
        return entries[index];
    }

    boolean inRange(int index) {return index>=0 && index<RingEntry.MAX_INDEX;}

    // for the full table there is no need to search any entry.
    public int indexOf(short index) {
        return inRange(index) ? index : -1;
    }

    public short ringIndex(int index) {

        assert inRange(index) : "invalid RingTable index: " + index;

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
