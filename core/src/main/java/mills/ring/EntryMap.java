package mills.ring;

import mills.util.AbstractRandomList;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/*
 * version:     $Revision$
 * created by:  dst
 * created on:  09.07.2014 20:06
 * modified by: $Author$
 * modified on: $Date$
 */

/**
 * Class entry map maps ring entries to values.
 * The map is unmodifiable if its values are unmodifiable.
 * The KeySet is unmodifiable anyway.
 * @param <T> type of map values.
 */
public class EntryMap<T> implements SortedMap<RingEntry, T> {

    protected final EntryTable keys;

    protected final List<T> values;

    public EntryMap(EntryTable keys, List<T> values) {
        this.keys = keys;
        this.values = values;

        if(keys.size() != values.size())
            throw new IllegalArgumentException("different sizes");
    }

    public T get(int index) {
        return values.get(index);
    }

    @Override
    public T put(RingEntry key, T value) {
        int index = keys.indexOf(key);
        if(index<0)
            throw new UnsupportedOperationException("key not found");
        return values.set(index, value);
    }

    @Override
    public Comparator<? super RingEntry> comparator() {
        return RingEntry.COMPARATOR;
    }

    public EntryMap<T> subMap(int fromIndex, int toIndex) {
        if(fromIndex==toIndex && fromIndex>=0 && fromIndex<size())
            return empty();

        final EntryTable subKeys = keys.subList(fromIndex, toIndex);
        final List<T> subValues = values.subList(fromIndex, toIndex);

        return newMap(subKeys, subValues);
    }

    @Override
    public EntryMap<T> subMap(RingEntry fromKey, RingEntry toKey) {

        int fromIndex = keys.lowerBound(fromKey.index);
        int toIndex   = keys.lowerBound(toKey.index);

        return subMap(fromIndex, toIndex);
    }

    @Override
    public EntryMap<T> headMap(RingEntry toKey) {
        return subMap(0, keys.lowerBound(toKey.index));
    }

    @Override
    public EntryMap<T> tailMap(RingEntry fromKey) {
        return subMap(keys.lowerBound(fromKey.index), size());
    }

    @Override
    public RingEntry firstKey() {
        return keys.first();
    }

    @Override
    public RingEntry lastKey() {
        return keys.last();
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public EntryTable keySet() {
        return keys;
    }

    public RingEntry key(int index) {
        return keys.get(index);
    }

    @Override
    public List<T> values() {
        return values;
    }

    public T value(int index) {
        return values.get(index);
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public T get(Object key) {
        int index = keys.indexOf(key);
        return index<0 ? null : values.get(index);
    }

    @Override
    public T remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends RingEntry, ? extends T> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public class Entry implements Map.Entry<RingEntry, T> {

        protected final int index;

        protected Entry(int index) {
            this.index = index;
        }

        @Override
        public RingEntry getKey() {
            return keys.get(index);
        }

        @Override
        public T getValue() {
            return values.get(index);
        }

        @Override
        public T setValue(T value) {
            return values.set(index, value);
        }
    }

    public static <T> EntryMap<T> preset(EntryTable keys, T value) {
        if(keys.isEmpty())
            return empty();

        List<T> values = AbstractRandomList.preset(keys.size(), value);
        return new EntryMap<>(keys, values);
    }

    /**
     * This may be overwritten to form derived sub maps.
     * @return a new EntryMap;
     */
    protected EntryMap<T> newMap(EntryTable keys, List<T> values) {
        return new EntryMap<>(keys, values);
    }

    private static final EntryMap<Object> EMPTY = new EntryMap<>(EntryTable.empty(), List.of());

    @SuppressWarnings("unchecked")
    public static <T> EntryMap<T> empty() {
        return (EntryMap<T>) EMPTY;
    }

    public <V> EntryMap<V> transform(Function<? super T, ? extends V> mapper) {
        List<V> newValues = AbstractRandomList.transform(this.values, mapper);
        return new EntryMap<>(this.keys, List.copyOf(newValues));
    }

    /**
     * Apply a filter in the key set and copy all values into a new EntryMap.
     * The created EntryMap is unmodifiable.
     * @param predicate to test keys.
     * @return a new EntryMap
     */
    public EntryMap<T> filter(Predicate<? super RingEntry> predicate) {
        EntryTable newKeys = keys.filter(predicate);
        if(newKeys.equals(keys))
            return this;

        if(newKeys.isEmpty())
            return empty();

        List<T> newValues = newKeys.stream().map(this::get).toList();

        return newMap(newKeys, newValues);
    }

    /**
     * Apply a filter on values.
     * @param predicate to test values.
     * @return a new EntryMap
     */
    public EntryMap<T> filterValues(Predicate<? super T> predicate) {
        return filter(key -> predicate.test(get(key)));
    }

    public class EntrySet extends AbstractRandomList<Map.Entry<RingEntry, T>> implements Set<Map.Entry<RingEntry, T>> {

        @Override
        public int size() {
            return keys.size();
        }

        @Override
        public Entry get(final int index) {
            return new Entry(index);
        }

        @Override
        public Spliterator<Map.Entry<RingEntry, T>> spliterator() {
            return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }
    }

    @Override
    public Set<Map.Entry<RingEntry, T>> entrySet() {
        return new EntrySet();
    }
}
