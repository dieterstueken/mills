package mills.ring;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.08.11
 * Time: 18:15
 */

class EmptyTable extends EntryList {

    static EmptyTable of() {
        return new EmptyTable();
    }

    @Override
    public EmptyTable filter(Predicate<? super RingEntry> predicate) {
        return this;
    }

    @Override
    public int size() {
        return 0;
    }

    public RingEntry get(int index) {
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public int findIndex(int ringIndex) {
        return -1;
    }

    @Override
    public int indexOf(RingEntry entry) {
        return -1;
    }

    public int findIndex(RingEntry entry) {
        return -1;
    }

    @Override
    public Iterator<RingEntry> iterator() {
        return Collections.emptyIterator();
    }

    public final RingEntry[] empty = new RingEntry[0];

    @Override
    public RingEntry[] toArray() {
        return empty;
    }

    @Override
    public int hashCode() {
        return -1;
    }

    @Override
    public Stream<RingEntry> stream() {
        return Stream.empty();
    }
}
