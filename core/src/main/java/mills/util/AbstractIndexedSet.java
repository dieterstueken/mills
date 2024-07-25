package mills.util;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 10:32 AM
 */
abstract public class AbstractIndexedSet<T extends Indexed> extends AbstractListSet<T> implements IndexedListSet<T> {

    @Override
    abstract public T get(int index);

    @Override
    abstract public int size();

    @Override
    public IndexedListSet<T> subList(int fromIndex, int toIndex) {

        int size = checkRange(fromIndex, toIndex);

        if(size==size())
            return this;

        if(fromIndex==0)
            return headSet(toIndex);

        return subSet(fromIndex, size);
    }

    abstract public IndexedListSet<T> subSet(int offset, int size);

    abstract public IndexedListSet<T> headSet(int toIndex);

    @Override
    public int lastIndexOf(final Object o) {
        // unique elements
        return indexOf(o);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof List<?> l))
            return false;

        if (l.size() != size())
            return false;

        for(int i=0; i<size(); ++i) {
            if(!Objects.equals(get(i), l.get(i)))
                return false;
        }

        return true;
    }

    private static final EmptyListSet<Indexed> EMPTY = new EmptyListSet<>();

    @SuppressWarnings("unchecked")
    public static <T extends Indexed> EmptyListSet<T> empty() {
        return (EmptyListSet<T>) EMPTY;
    }

    public static class SingletonSet<T extends Indexed> extends AbstractIndexedSet<T> {

        final T value;

        public SingletonSet(T value) {
            this.value = value;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public int indexOf(Object obj) {
            return Objects.equals(obj, value) ? 0 : -1;
        }

        @Override
        public IndexedListSet<T> subSet(int fromIndex, int size) {

            if(fromIndex==0)
                return headSet(size);

            throw new IllegalArgumentException("Size = " + size);
        }

        @Override
        public IndexedListSet<T> headSet(int size) {

            if(size==0)
                return empty();

            if(size==1)
                return this;

            throw new IllegalArgumentException("Size = " + size);
        }

        @Override
        public T get(int index) {
            if (index == 0)
                return value;
            else
                return AbstractIndexedSet.<T>empty().get(index);
        }

        @Override
        public T getFirst() {
            return value;
        }

        @Override
        public Iterator<T> iterator() {
            //return Iterators.singletonIterator(entry());
            return super.iterator();
        }

        @Override
        public Stream<T> stream() {
            return Stream.of(value);
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            action.accept(value);
        }

        @Override
        public int hashCode() {
            return 31 + value.hashCode();
        }
    }

    public static <T extends Indexed> IndexedListSet<T> singleton(T value) {
        return new SingletonSet<>(value);
    }
}
