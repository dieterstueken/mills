package mills.util.listset;

import mills.util.AbstractRandomList;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  14.02.2021 19:15
 * modified by: $
 * modified on: $
 */

/**
 * Class ListMap associates a list of values to a list of keys to form a map.
 * The keyset is assumed to be immutable.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class ListMap<K, V> extends AbstractMap<K, V> {

    protected final ListSet<K> keySet;

    protected final List<V> values;

    protected ListMap(ListSet<K> keySet, List<V> values) {
        this.keySet = keySet;
        this.values = values;
    }

    public static <K, V> ListMap<K, V> create(ListSet<K> keySet, List<V> values) {
        return new ListMap<>(keySet, values);
    }

    @Override
    public int size() {
        return keySet.size();
    }

    public K getKey(int index) {
        return keySet.get(index);
    }

    public V getValue(int index) {
        if(index>=0 && index<values.size())
            return values.get(index);

        return defaultValue();
    }

    protected V defaultValue() {
        return null;
    }

    public Entry<K, V> getEntry(int index) {
        return new SimpleImmutableEntry<>(getKey(index), getValue(index));
    }

    @Override
    public ListSet<K> keySet() {
        return keySet;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        var entries= AbstractRandomList.generate(size(), this::getEntry);
        final Comparator<? super K> comparator = keySet.comparator();
        return ListSet.of(entries, comparator==null ? null : Entry.comparingByKey(comparator));
    }

    @Override
    public boolean containsKey(Object key) {
        return keySet.contains(key);
    }

    @Override
    public V get(Object key) {
        int index = keySet.indexOf(key);
        return getValue(index);
    }

    @Override
    public List<V> values() {
        return values;
    }
}
