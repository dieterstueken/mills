package mills.util;

import java.util.*;
import java.util.function.IntFunction;

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
        return RandomList.super.getFirst();
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

    static <T extends Indexed> ListSet<T> ofIndexed(List<T> values) {
        return IndexedListSet.of(values);
    }

    static <T extends Indexed> IndexedListSet<T> ifDirect(List<T> values) {
        return IndexedListSet.ifDirect(values);
    }

    static <T> ListSet<T> of(List<T> values, Comparator<? super T> comparator) {
        return DelegateListSet.of(values, comparator);
    }

    static <T extends Indexed> ListSet<T> ofIndexed(int size, IntFunction<? extends T> generate) {
        return ofIndexed(AbstractRandomList.generate(size, generate));
    }

    static <T extends Indexed> ListSet<T> ofDirect(List<T> values) {
        return DirectListSet.of(values, Indexer.INDEXED);
    }

    static <T> ListSet<T> ofIndexed(int size, IntFunction<? extends T> generate, Indexer<T> indexer) {
        return IndexedDelegate.of(AbstractRandomList.generate(size, generate), indexer);
    }

    static <T extends Comparable<? super T>> ListSet<T> of(List<T> values) {
        // fast track
        if(values instanceof ListSet)
            return (ListSet<T>)values;

        return of(values, Comparator.naturalOrder());
    }

    static <I extends Indexed> ListSet<I> of(I[] values) {
        return DirectListSet.of(values, Indexer.INDEXED);
    }

    static <E extends Enum<E>> ListSet<E> of(Class<E> type) {
        return DirectListSet.of(type.getEnumConstants(), Indexer.ENUM);
    }

    default <V> ListMap<T, V> mapOf(List<V> values) {
        return ListMap.create(this, values);
    }
}
