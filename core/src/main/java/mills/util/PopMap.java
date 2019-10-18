package mills.util;

import mills.bits.PopCount;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  18.09.2019 10:41
 * modified by: $
 * modified on: $
 */

/**
 * Class PopMap implements an immutable map of PopCounts.
 * @param <T> the type of mapped values.
 */
public class PopMap<T>  {

    private final Map<PopCount, T> values;

    protected PopMap(List<T> values) {
        this.values = ArraySet.of(PopCount::get, values, null).asMap();
    }

    public Collection<T> values() {
        return values.values();
    }

    public T get(Object key) {
        return key instanceof PopCount ? get((PopCount) key) : null;
    }

    public T get(int index) {
        return values.get(PopCount.get(index));
    }

    public T get(PopCount pop) {
        return values.get(pop);
    }
}
