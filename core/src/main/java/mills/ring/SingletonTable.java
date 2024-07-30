package mills.ring;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.08.11
 * Time: 18:16
 */
class SingletonTable extends AbstractEntryTable implements IndexedEntryTable {

    final RingEntry entry;

    private SingletonTable(RingEntry entry) {
        this.entry = entry;
    }

    public static SingletonTable of(RingEntry entry) {
        return entry.singleton();
    }

    public static SingletonTable of(int index) {
        return Entries.entry(index).singleton();
    }

    static SingletonTable create(RingEntry entry) {
        if(entry.index==0)
            return new Direct(entry);
        else
            return new SingletonTable(entry);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int getIndex() {
        return entry.index;
    }

    @Override
    public EntryTable filter(Predicate<? super RingEntry> predicate) {
        if (predicate.test(entry))
            return this;
        else
            return EntryTable.empty();
    }

    @Override
    public RingEntry get(int index) {
        if (index == 0)
            return entry;
        else
            throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public EntryTable headSet(final RingEntry toElement) {
        if(entry.index<toElement.index)
            return this;
        else
            return EntryTable.empty();
    }

    @Override
    public EntryTable headSet(final int size) {

        if (size == 0)
            return EntryTable.empty();

        if (size == 1)
            return this;

        throw new IllegalArgumentException("Size = " + size);
    }

    @Override
    public EntryTable tailSet(final RingEntry toElement) {
        if(entry.index>=toElement.index)
            return this;
        else
            return EntryTable.empty();
    }

    @Override
    public RingEntry getFirst() {
        return entry;
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

    public int findIndex(RingEntry entry) {
        return findIndex(entry.index());
    }

    @Override
    public int indexOf(Object obj) {
        return Objects.equals(obj, entry) ? 0 : -1;
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
    public void forEach(Consumer<? super RingEntry> action) {
        action.accept(entry);
    }

    @Override
    public int hashCode() {
        return entry.hashCode();
    }

    public static class Direct extends SingletonTable implements DirectTable {

        Direct(final RingEntry entry) {
            super(entry);
            assert entry.index==0;
        }

        @Override
        public DirectTable headSet(final RingEntry toElement) {
            if(entry.index<toElement.index)
                return this;
            else
                return EntryTable.empty();
        }

        @Override
        public DirectTable headSet(final int size) {

            if (size == 0)
                return EntryTable.empty();

            if (size == 1)
                return this;

            throw new IllegalArgumentException("Size = " + size);
        }
    }
}
