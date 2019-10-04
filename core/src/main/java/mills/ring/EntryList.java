package mills.ring;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.10.19
 * Time: 01:01
 */
abstract public class EntryList extends EntryTable {

    public RingEntry getEntry(int index) {
        return Entries.of(index);
    }
    
    public short ringIndex(int i) {
        return get(i).index;
    }
}
