package mills.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 10:32 AM
 */
abstract public class AbstractListSet<T> extends AbstractList<T> implements ListSet<T> {

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

    protected AbstractListSet<T> verify() {
        assert isOrdered(this, comparator()) : "index mismatch";
        return this;
    }

    static <T> boolean isOrdered(List<T> values, Comparator<? super T> order) {

        if(values.size()<2)
            return true;

        T t0 = values.get(0);
        for (int i = 1; i < values.size(); ++i) {
            T t1 = values.get(i);
            if(order.compare(t0, t1)>=0)
                return false;
            t0 = t1;
        }

        return  true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof List))
            return false;

        List<?> l = (List) o;

        if (l.size() != size())
            return false;

        for(int i=0; i<size(); ++i) {
            if(!Objects.equals(get(i), l.get(i)))
                return false;
        }

        return true;
    }

    public static <T> ListSet<T> of(T[] values, Comparator<? super T> comparator) {
        return of(Arrays.asList(values), comparator);
    }

    public static <T> ListSet<T> of(List<T> values, Comparator<? super T> comparator) {

        assert isOrdered(values, comparator) : "index mismatch";

        return new AbstractListSet<>() {

            @Override
            public T get(int index) {
                return values.get(index);
            }

            @Override
            public int size() {
                return values.size();
            }

            @Override
            public ListSet<T> subList(int fromIndex, int toIndex) {
                return of(values.subList(fromIndex, toIndex), comparator);
            }

            @Override
            public Comparator<? super T> comparator() {
                return comparator;
            }

            @Override
            public int checkRange(int fromIndex, int toIndex) {
                return super.checkRange(fromIndex, toIndex);
            }

            @Override
            public boolean add(T t) {
                if(isEmpty()) {
                    values.add(t);
                } else {
                    int index = findIndex(t);

                    // already contained
                    if (index >= 0)
                        return false;

                    values.add(-index - 1, t);
                }

                return true;
            }

            @Override
            public void clear() {
                values.clear();
            }

            @Override
            public boolean remove(Object o) {
                int index = findIndex((T)o);
                if(index<0)
                    return false;

                values.remove(index);

                return true;
            }

            @Override
            public T remove(int index) {
                return values.remove(index);
            }
        };
    }

    protected static final AbstractListSet<Object> EMPTY = new AbstractListSet<Object>() {

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
    public static <T> AbstractListSet<T> empty() {
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
