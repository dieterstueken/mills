package mills.util;

import java.util.*;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.10.19
 * Time: 19:52
 */
public interface ListSet<T> extends List<T>, SortedSet<T>, RandomAccess {

    @Override
    default boolean contains(Object obj) {
            return indexOf(obj) >= 0;
        }

    /**
     *
     * @param key the key to be searched for.
     * @return the index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.
     */
    default int findIndex(T key) {
        return Collections.binarySearch(this, key, comparator());
    }

    /**
     * Return the index of the first element.index which is greater or equal than ringIndex.
     * The returned index is between [0, size]
     * @param key to find.
     * @return index of the first element which is greater than ringIndex.
     */
    default int lowerBound(T key) {
        int index = findIndex(key);
        return index<0 ? -(index+1) : index;
    }

    /**
     * Return the index of the first element.index which is strictly greater than ringIndex.
     * The returned index is between [0, size]
     * @param key to find.
     * @return index of the first element which is greater than ringIndex.
     */
    default int upperBound(T key) {
        int index = findIndex(key);
        return index<0 ? -(index+1) : index+1;
    }

    @Override
    default T first() {

        if(isEmpty())
            throw new NoSuchElementException();

        return get(0);
    }

    @Override
    default T last() {
        if(isEmpty())
            throw new NoSuchElementException();

        return get(size() - 1);
    }

    @Override
    default ListSet<T> subSet(T fromElement, T toElement) {
        return subList(lowerBound(fromElement), lowerBound(toElement));
    }

    @Override
    default ListSet<T> headSet(T toElement) {
        return subList(0, lowerBound(toElement));
    }

    @Override
    default ListSet<T> tailSet(T fromElement) {
        return subList(lowerBound(fromElement), size());
    }

    @Override
    ListSet<T> subList(int fromIndex, int toIndex);

    default ListSet<T> subList(int toIndex) {
        return subList(0, toIndex);
    }

    int checkRange(int fromIndex, int toIndex);

    @Override
    default Spliterator<T> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    default <E> ListSet<E> transform(Function<? super T, ? extends E> mapper, Comparator<? super E> comparator) {
        List<E> transformed = AbstractRandomList.transform(this, mapper);
        return AbstractListSet.of(transformed, comparator);
    }

    static <T> ListSet<T> of(List<T> values, Comparator<? super T> comparator) {
        return AbstractListSet.of(values, comparator);
    }

    static <T extends Indexed> ListSet<T> of(List<T> values) {
        return AbstractListSet.of(values, Indexer.INDEXED);
    }

    static <T extends Indexed> ListSet<T> of(T[] values) {
        return AbstractListSet.of(List.of(values), Indexer.INDEXED);
    }

    static <T extends Enum<T>> ListSet<T> of(T[] values) {
        return AbstractListSet.of(List.of(values), Indexer.ENUM);
    }
}
