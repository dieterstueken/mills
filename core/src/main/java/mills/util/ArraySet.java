package mills.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  18.10.2019 13:43
 * modified by: $
 * modified on: $
 */

public class ArraySet<K extends Indexed, V> extends AbstractSet<Map.Entry<K, V>> {

    public static <K extends Indexed, V> ArraySet<K,V> of(IntFunction<K> keys, List<V> values, V defaultValue) {
        return new ArraySet<>(keys, values, defaultValue);
    }

    public static <K extends Indexed, V> Map<K,V> mapOf(IntFunction<K> keys, List<V> values, V defaultValue) {
        return of(keys, values, defaultValue).asMap();
    }

    public static <K extends Indexed, V> Map<K,V> mapOf(List<V> values, Function<V, K> getKey, V defaultValue) {
        return of(i->getKey.apply(values.get(i)), values, defaultValue).asMap();
    }

    public static <K extends Indexed, V> Map<K,V> mapOf(List<K> keys, V defaultValue) {
        List<V> values = AbstractRandomArray.preset(keys.size(), defaultValue);
        return of(keys::get, values, defaultValue).asMap();
    }

    public Map<K,V> asMap() {
        return new ArrayMap();
    }

    private final IntFunction<K> keys;
    private final List<V> values;
    private final V defaultValue;
    private int size;

    private ArraySet(IntFunction<K> keys, List<V> values, V defaultValue) {
        this.keys = keys;
        this.values = values;
        this.defaultValue = defaultValue;

        int size=0;
        for (V value : values) {
            if (isValue(value))
                ++size;
        }

        this.size = size;
    }

    private boolean isValue(V value) {
        return value != defaultValue && value!=null;
    }

    @Override
    public int size() {
        return size;
    }

    public V put(K key, V value) {
        int index = key.getIndex();

        V prev = values.set(index, value);

        int n = isValue(value) ? 1 : 0;
        n -= isValue(prev) ? 1 : 0;

        if(n!=0)
            size += n;

        assert n>=0 && n<values.size();

        return prev;
    }

    public V get(int index) {
        V value = values.get(index);
        return value!=null ? value : defaultValue;
    }

    public V get(Object key) {

        if(key instanceof Indexed) {
            int index = ((Indexed)key).getIndex();
            assert keys.apply(index).equals(key);
            return get(index);
        }

        return null;
    }

    @Override
    public boolean contains(Object key) {
        V value = get(key);
        return value!=null && value != defaultValue;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {

        return new Iterator<>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                // seek to next value
                while(index < values.size()) {
                    if(get(index)!=defaultValue)
                        return true;
                    else
                        ++index;
                }

                return false;
            }

            @Override
            public Map.Entry<K, V> next() {
                if(!hasNext())
                    throw new NoSuchElementException();

                K key = keys.apply(index);
                V value = values.get(index);
                ++index;

                return new AbstractMap.SimpleImmutableEntry<>(key, value);
            }
        };
    }

    class ArrayMap extends AbstractMap<K,V> {
        @Override
        public Set<K> keySet() {
            return super.keySet();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return ArraySet.this;
        }

        @Override
        public V get(Object key) {
            return ArraySet.this.get(key);
        }

        @Override
        public V put(K key, V value) {
            return ArraySet.this.put(key, value);
        }

        @Override
        public boolean containsKey(Object key) {
            return ArraySet.this.contains(key);
        }
    }

}
