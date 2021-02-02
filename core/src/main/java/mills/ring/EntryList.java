package mills.ring;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.10.19
 * Time: 01:01
 */
abstract class EntryList extends AbstractEntryTable {

    public RingEntry getEntry(int index) {
        return Entries.of(index);
    }
    
    public short ringIndex(int i) {
        return get(i).index;
    }
}
