package mills.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 10:32 AM
 */
abstract public class ListSet<T> extends AbstractRandomList<T> implements SortedSet<T> {

    public static <E extends Enum<E>> ListSet<E> of(Class<E> type) {

        E[] values = type.getEnumConstants();

        return new ListSet<E>() {

            @Override
            public int indexOf(Object o) {
                return type.isInstance(o) ? type.cast(o).ordinal() : -1;
            }

            @Override
            public E get(int index) {
                return values[index];
            }

            @Override
            public int size() {
                return values.length;
            }
        };
    }

    public static <T extends Indexed> ListSet<T> indexed(T[] values) {

        return new IndexedSet<T>() {

            @Override
            public T get(int index) {
                return values[index];
            }

            @Override
            public int size() {
                return values.length;
            }
        }.verify();
    }

    public static <T extends Indexed> ListSet<T> indexed(List<T> values) {

        return new IndexedSet<T>() {

            @Override
            public T get(int index) {
                return values.get(index);
            }

            @Override
            public int size() {
                return values.size();
            }
        }.verify();
    }

    abstract static class IndexedSet<T extends Indexed> extends ListSet<T> {

        @Override
        public int indexOf(Object o) {
            if(o instanceof Indexed) {
                int key = ((Indexed)o).getIndex();
                return Indexed.INDEXER.binarySearchKey(this, key);
            } else
                return super.indexOf(o);
        };

        @Override
        public Indexer<? super T> comparator() {
            return Indexed.INDEXER;
        }
    }

    public <X> ListSet<X> transform(Function<? super T, ? extends X> mapper) {
        return new ListSet<>() {

            @Override
            public X get(int index) {
                T t = ListSet.this.get(index);
                return mapper.apply(t);
            }

            @Override
            public int size() {
                return ListSet.this.size();
            }
        };
    }

    @Override
    abstract public T get(int index);

    @Override
    abstract public int size();

    @Override
    public boolean contains(Object obj) {
            return indexOf(obj) >= 0;
        }

    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    ListSet<T> verify() {
        assert isOrdered(comparator()) : "index mismatch";
        return this;
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

        return partition(fromIndex, range);
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

    private static final ListSet<?> EMPTY = new Empty<>();

    @SuppressWarnings("unchecked")
    protected ListSet<T> empty() {
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
        public int findIndex(Object key) {
            return value.equals(key) ? 1 : -1;
        }

        @Override
        public Iterator<T> iterator() {
            //return Iterators.singletonIterator(entry());
            return super.iterator();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            action.accept(value);
        }

        @Override
        public int hashCode() {
               return value.hashCode();
           }
    }

    protected ListSet<T> singleton(final T value) {
        Comparator<? super T>  cmp = comparator();

        if(cmp==null)
            return new Singleton<>(value);
        else
            return new Singleton<>(value) {
                @Override
                public Comparator<? super T> comparator() {
                    return cmp;
                }
            };
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
                return Math.max(index, limit);
            }

            // index >= offset
            index -= offset;
            return index<size ? index : -(size+1);
        }

        @Override
        protected ListSet<T> partition(int fromIndex, int range) {
            // Prevent from extending the range beyond given bounds.
            checkRange(fromIndex, fromIndex+range);

            // no cascading sublists, delegate to parent
            return parent.partition(fromIndex + offset, range);
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

    // called internally if not empty nor singleton
    // override to return appropriated type
    protected ListSet<T> partition(int fromIndex, int range) {
        return new SubSet<>(this, fromIndex, range);
    }
}
