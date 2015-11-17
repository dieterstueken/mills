package mills.ring;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.08.11
 * Time: 18:16
 */
class SingleEntry extends EntryTable {

    final RingEntry entry;

    SingleEntry(RingEntry entry) {
        this.entry = entry;
    }

    public static SingleEntry of(RingEntry entry) {
        return entry.singleton;
    }

    public static SingleEntry of(int index) {
        return RingEntry.of(index).singleton;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public EntryTable filter(Predicate<? super RingEntry> predicate) {
        if (predicate.test(entry))
            return this;
        else
            return EMPTY;
    }

    @Override
    public RingEntry get(int index) {
        if (index == 0)
            return entry;
        else
            throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public int findIndex(int ringIndex) {
        int i = entry.index();
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
    public Stream<RingEntry> stream() {
        return Stream.of(entry);
    }

    @Override
    public int hashCode() {
        return 31 + entry.hashCode();
    }
}
