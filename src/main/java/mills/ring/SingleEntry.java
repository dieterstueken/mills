package mills.ring;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.Iterator;

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
        if (predicate.apply(entry()))
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
    public int indexOf(short ringIndex) {
        int i = entry().index();
        if(i==ringIndex)
            return 0;
        if(ringIndex<i)
            return -1;
        return -2;
    }

    @Override
    public EntryTable subList(int fromIndex, int toIndex) {
        if(fromIndex==0 && toIndex==1)
            return this;

        throw new IllegalArgumentException("EmptyTable.subList");
    }

    @Override
    public Iterator<RingEntry> iterator() {
        return Iterators.singletonIterator(entry());
    }

    @Override
    public int hashCode() {
        return entry().hashCode();
    }
}
