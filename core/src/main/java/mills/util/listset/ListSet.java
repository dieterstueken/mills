package mills.util.listset;

import mills.util.Indexed;
import mills.util.RandomList;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.10.19
 * Time: 19:52
 */
public interface ListSet<T> extends RandomList<T>, SortedSet<T> {

    @Override
    Comparator<? super T> comparator();

    @Override
    default boolean contains(Object obj) {
        return indexOf(obj) >= 0;
    }

    /**
     *
     * @param entry the entry to be searched for.
     * @return the index of the search entry, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.
     */
    default int findIndex(T entry) {
        if(isEmpty())
            return -1;

        Comparator<? super T> comparator = comparator();
        int size = size();
        T last = get(size-1);
        int cmp = comparator.compare(entry, last);

        if(cmp<0) // within
            return Collections.binarySearch(this, entry, comparator);

        // at end or beyond
        return cmp==0 ? size-1 : -size-1;
    }

    /**
     * This may be overridden to speed up for direct access.
     * @param o element to search for
     * @return index of the requested element or -1 if missing.
     */
    @Override
    int indexOf(Object o);

    @Override
    default int lastIndexOf(Object o) {
        // must be unique
        return indexOf(o);
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

    ListSet<T> subSet(int offset, int size);

    @Override
    default ListSet<T> headSet(T toElement) {
        return headSet(lowerBound(toElement));
    }

    default ListSet<T> headSet(int toIndex) {
        return subList(0, toIndex);
    }

    @Override
    default ListSet<T> tailSet(T fromElement) {
        return subList(lowerBound(fromElement), size());
    }

    @Override
    ListSet<T> subList(int fromIndex, int toIndex);

    default void checkIndex(int index) {
        if(index<0 || index>=size())
            throw new IndexOutOfBoundsException("Index = " + index);
    }

    default boolean inRange(int index) {
        return index >= 0 && index < size();
    }

    default int checkRange(int fromIndex, int toIndex) {

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
    default Spliterator<T> spliterator() {
        // prefer RandomAccessSpliterator
        return RandomList.super.spliterator();
    }

    @Override
    default ListSet<T> reversed() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void addFirst(T t) {
        RandomList.super.addFirst(t);
    }

    @Override
    default void addLast(T t) {
        RandomList.super.addLast(t);
    }

    @Override
    default T getFirst() {
        return this.get(0);
    }

    @Override
    default T getLast() {
        return RandomList.super.getLast();
    }

    @Override
    default T removeFirst() {
        return RandomList.super.removeFirst();
    }

    @Override
    default T removeLast() {
        return RandomList.super.removeLast();
    }

    static <T> ListSet<T> of(Comparator<? super T> comparator) {
        return of(new ArrayList<>(), comparator);
    }

    static <T extends Comparable<T>> ListSet<T> of() {
        return of(Comparator.naturalOrder());
    }

    static <T> ListSet<T> of(List<T> values, Comparator<? super T> comparator) {
        return DelegateListSet.of(values, comparator);
    }

    static <T extends Comparable<? super T>> ListSet<T> of(List<T> values) {
        // fast track
        if(values instanceof ListSet)
            return (ListSet<T>)values;

        return of(values, Comparator.naturalOrder());
    }

    static <I extends Indexed> ListSet<I> of(I[] values) {
        if(DirectListSet.isDirect(values))
            return DirectArraySet.of(values, values.length);
        else
            return new ArraySet<>(values);
    }

    default <V> ListMap<T, V> mapOf(List<V> values) {
        return ListMap.create(this, values);
    }


    static <T> boolean isOrdered(T[] values, Comparator<? super T> order) {

        if(values.length<2)
            return true;

        T t0 = values[0];
        for (int i = 1; i < values.length; ++i) {
            T t1 = values[i];
            if(order.compare(t0, t1)>=0)
                return false;
            t0 = t1;
        }

        return  true;
    }
}
