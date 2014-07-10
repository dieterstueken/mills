package mills.ring;

import mills.util.AbstractRandomList;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.07.2014 20:06
 * modified by: $Author$
 * modified on: $Date$
 */
public class EntryMap<T> extends AbstractMap<RingEntry, T> implements SortedMap<RingEntry, T> {

    protected final EntryTable keys;

    protected final List<T> values;

    protected EntryMap(EntryTable keys, List<T> values) {
        this.keys = keys;
        this.values = values;

        if(keys.size() != values.size())
            throw new IllegalArgumentException("different sizes");
    }

    @Override
    public Comparator<? super RingEntry> comparator() {
        return RingEntry.COMPARATOR;
    }

    public SortedMap<RingEntry, T> subMap(int fromIndex, int toIndex) {
        final EntryTable subKeys = keys.subList(fromIndex, toIndex);
        final List<T> subValues = values.subList(fromIndex, toIndex);
        return new EntryMap<T>(subKeys, subValues);
    }

    @Override
    public SortedMap<RingEntry, T> subMap(RingEntry fromKey, RingEntry toKey) {

        int fromIndex = keys.lowerBound(fromKey.index);
        int toIndex   = keys.lowerBound(toKey.index);

        return subMap(fromIndex, toIndex);
    }

    @Override
    public SortedMap<RingEntry, T> headMap(RingEntry toKey) {
        return subMap(0, keys.upperBound(toKey.index));
    }

    @Override
    public SortedMap<RingEntry, T> tailMap(RingEntry fromKey) {
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

    private class Entries extends AbstractRandomList<Entry<RingEntry, T>> implements Set<Entry<RingEntry, T>> {

        @Override
        public int size() {
            return keys.size();
        }

        @Override
        public Entry<RingEntry, T> get(final int index) {
            return new Entry<RingEntry, T>() {

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
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Spliterator<Entry<RingEntry, T>> spliterator() {
            return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }
    }

    @Override
    public Set<Entry<RingEntry, T>> entrySet() {
        return new Entries();
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public EntryTable keySet() {
        return keys;
    }

    @Override
    public List<T> values() {
        return values;
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public T get(Object key) {
        int index = keys.indexOf(key);
        return index<0 ? null : values.get(index);
    }

    @Override
    public void forEach(BiConsumer<? super RingEntry, ? super T> action) {
        // todo: loop by index?
        super.forEach(action);
    }
}
