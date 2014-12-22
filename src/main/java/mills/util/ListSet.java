package mills.util;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 10:32 AM
 */
abstract public class ListSet<T> extends AbstractRandomList<T> implements SortedSet<T> {

    @Override
    public boolean contains(Object obj) {
            return indexOf(obj) >= 0;
        }

    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    /**
     *
     * @param key the key to be searched for.
     * @return the index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.
     */
    public int findIndex(T key) {
        return Collections.binarySearch(this, key, comparator());
    }

    /**
     * Return the index of the first element.index which is greater or equal than ringIndex.
     * The returned index is between [0, size]
     * @param key to find.
     * @return index of the first element which is greater than ringIndex.
     */
    public int lowerBound(T key) {
        int index = findIndex(key);
        return index<0 ? -(index+1) : index;
    }

    /**
     * Return the index of the first element.index which is strictly greater than ringIndex.
     * The returned index is between [0, size]
     * @param key to find.
     * @return index of the first element which is greater than ringIndex.
     */
    public int upperBound(T key) {
        int index = findIndex(key);
        return index<0 ? -(index+1) : index+1;
    }

    protected int checkRange(int fromIndex, int toIndex) {

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
    public T first() {

        if(isEmpty())
            throw new NoSuchElementException();

        return get(0);
    }

    @Override
    public T last() {
        if(isEmpty())
            throw new NoSuchElementException();

        return get(size() - 1);
    }

    @Override
    public ListSet<T> subSet(T fromElement, T toElement) {
        return subList(lowerBound(fromElement), lowerBound(toElement));
    }

    @Override
    public ListSet<T> headSet(T toElement) {
        return subList(0, lowerBound(toElement));
    }

    @Override
    public ListSet<T> tailSet(T fromElement) {
        return subList(lowerBound(fromElement), size());
    }

    @Override
    public ListSet<T> subList(int fromIndex, int toIndex) {

        int range = checkRange(fromIndex, toIndex);

        if(range==0)
            return empty();

        if(range==1)
            return singleton(get(fromIndex));

        if(range==size())
            return this;

        return subset(fromIndex, range);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    protected static class Empty<T> extends ListSet<T> {

        @Override
        public int size() {
            return 0;
        }

        public T get(int index) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        @Override
        public int findIndex(Object key) {
            return -1;
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.emptyIterator();
        }
    }

    private static final ListSet<?> EMPTY = new Empty<Object>();

    @SuppressWarnings("unchecked")
    public <T> ListSet<T> empty() {
        return (ListSet<T>) EMPTY;
    }

    protected static class Singleton<T> extends ListSet<T> {

        final T value;

        protected Singleton(T value) {
            this.value = value;
        }

        @Override
        public int size() {
                return 1;
            }

        @Override
        public T get(int index) {
            if (index == 0)
                return value;
            else
                throw new IndexOutOfBoundsException("Index: " + index);
        }

        @Override
        public Iterator<T> iterator() {
            //return Iterators.singletonIterator(entry());
            return super.iterator();
        }

        @Override
        public int hashCode() {
               return value.hashCode();
           }
    }

    public ListSet<T> singleton(final T value) {
        return new Singleton<T>(value);
    }

    protected static class SubSet<T> extends ListSet<T> {

        final ListSet<T> parent;

        final int offset;

        final int size;

        protected SubSet(ListSet<T> parent, int offset, int size) {
            this.parent = parent;
            this.offset = offset;
            this.size = size;
        }

        @Override
        public Comparator<? super T> comparator() {
            return parent.comparator();
        }

        @Override
        public int findIndex(T key) {
            int index = parent.findIndex(key);

            if(index<offset) {
                index+=offset;
                if(index>=0) // was between [0,offset[
                    return -1;

                // was negative, limit negative size.
                int limit = -(size+1);
                return index<limit ? limit : index;
            }

            // index >= offset
            index -= offset;
            return index<size ? index : -(size+1);
        }

        @Override
        public ListSet<T> subList(int fromIndex, int toIndex) {

            // Prevent from extending the range beyond given bounds.
            checkRange(fromIndex, toIndex);

            return parent.subList(fromIndex + offset, toIndex + offset);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public T get(int index) {
            return parent.get(index+offset);
        }
    }

    protected ListSet<T> subset(int fromIndex, int toIndex) {
        return new SubSet<T>(this, fromIndex, toIndex);
    }
}
