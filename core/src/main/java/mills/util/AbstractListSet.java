package mills.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 10:32 AM
 */
abstract public class AbstractListSet<T> extends AbstractRandomList<T> implements ListSet<T> {

    @Override
    abstract public T get(int index);

    @Override
    abstract public int size();

    @Override
    abstract public ListSet<T> subList(int fromIndex, int toIndex);

    public int checkRange(int fromIndex, int toIndex) {

        if(fromIndex<0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);

        if (toIndex > size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);

        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");

        return toIndex-fromIndex;
    }

    @Override
    public int lastIndexOf(final Object o) {
        // unique elements
        return indexOf(o);
    }

    public boolean inRange(int index) {
        return index >= 0 && index < MAX_INDEX;
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

    private static final AbstractListSet<Object> EMPTY = new AbstractListSet<>() {

        @Override
        public Object get(int index) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public ListSet<Object> subList(int fromIndex, int toIndex) {
            checkRange(fromIndex, toIndex);
            return this;
        }

        @Override
        public Stream<Object> stream() {
            return Stream.of();
        }

        @Override
        public void forEach(Consumer<? super Object> action) {
        }

        @Override
        public int hashCode() {
            return Collections.emptyList().hashCode();
        }

        @Override
        public Comparator<Object> comparator() {
            return null;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> ListSet<T> empty() {
        return (AbstractListSet<T>) EMPTY;
    }

    public static class Singleton<T> extends AbstractListSet<T> {

        final T value;

        public Singleton(T value) {
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
        public ListSet<T> subList(int fromIndex, int toIndex) {
            int size = checkRange(fromIndex, toIndex);

            if(size==0)
                return empty();

            return this;
        }

        @Override
        public T get(int index) {
            if (index == 0)
                return value;
            else
                return AbstractListSet.<T>empty().get(index);
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

        @Override
        public Comparator<? super T> comparator() {
            return null;
        }
    }

    public static <T> ListSet<T> singleton(T value) {
        return new Singleton<>(value);
    }
}
