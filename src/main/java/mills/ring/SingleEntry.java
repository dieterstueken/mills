package mills.ring;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.08.11
 * Time: 18:16
 */
abstract class SingleEntry extends EntryTable {

    abstract RingEntry entry();

    public static SingleEntry of(RingEntry entry) {
        return entry.singleton;
    }

    public static SingleEntry of(int index) {
        return of(RingEntry.of(index));
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public EntryTable filter(Predicate<? super RingEntry> predicate) {
        if (predicate.test(entry()))
            return this;
        else
            return EMPTY;
    }

    @Override
    public RingEntry get(int index) {
        if (index == 0)
            return entry();
        else
            throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public int findIndex(int ringIndex) {
        int i = entry().index();
        if(i==ringIndex)
            return 0;
        if(ringIndex<i)
            return -1;
        return -2;
    }

    @Override
    public Iterator<RingEntry> iterator() {
        //return Iterators.singletonIterator(entry());
        return super.iterator();
    }

    @Override
    public int hashCode() {
        return entry().hashCode();
    }
}
