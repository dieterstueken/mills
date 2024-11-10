package mills.ring;

import java.util.function.Predicate;

import static mills.ring.Entries.entry;
import static mills.ring.Entries.validateIndex;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.24
 * Time: 18:08
 */
public class PairTable extends AbstractEntryTable {

    final short i0;
    final short i1;

    PairTable of(RingEntry r0, RingEntry r1) {
        return new PairTable(r0.index, r1.index);
    }

    private PairTable of(int r0, int r1) {
        return new PairTable(validateIndex(r0), validateIndex(r1));
    }

    private PairTable(short i0, short i1) {
        if(i1<=i0)
            throw new IllegalArgumentException("unordered PairTable");

       this.i0 = i0;
       this.i1 = i1;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public RingEntry get(final int index) {
        if(index==0)
            return entry(i0);

        if(index==1)
            return entry(i1);

        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public int findIndex(final int ringIndex) {

        if(ringIndex<i0)
            return -1;

        if(ringIndex==i0)
            return 0;

        if(ringIndex==i1)
            return 1;

        if(ringIndex<i1)
            return -2;

        return -3;
    }

    @Override
    public EntryTable headSet(final RingEntry toElement) {
        return super.headSet(toElement);
    }

    @Override
    public EntryTable headList(final int size) {
        return super.headList(size);
    }

    @Override
    public EntryTable tailSet(final RingEntry fromElement) {
        return super.tailSet(fromElement);
    }

    @Override
    public EntryTable filter(final Predicate<? super RingEntry> predicate) {
        RingEntry e0 = entry(i0);
        RingEntry e1 = entry(i1);

        if (predicate.test(e0)) {
            if(predicate.test(e1))
                return this;
            else
                return e1.singleton();
        } else {
            if (predicate.test(e1))
                return e1.singleton();
        }

        return EntryTable.empty();
    }
}
