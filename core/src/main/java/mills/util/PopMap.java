package mills.util;

import mills.bits.PopCount;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

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
public class PopMap<T> extends AbstractMap<PopCount, T> {

    private ListSet<PopCount> keySet;

    private final List<T> values;
    
    private final ListSet<Entry<PopCount, T>> entries;

    protected PopMap(List<T> values, ListSet<Entry<PopCount, T>> entries) {
        this.keySet = PopCount.TABLE.subList(0, values.size());
        this.values = values;

        // create virtual entry set
        if(entries==null)
            entries = keySet.transform(pop -> new SimpleImmutableEntry<>(pop, values.get(pop.index)));

        this.entries = entries;
    }

    protected PopMap(List<T> tables) {
        this(tables, null);
    }

    // todo: must deduce keySet first
    //protected PopMap(ListSet<Entry<PopCount, T>> entries) {
    //    this(AbstractRandomList.transform(entries, Map.Entry::getValue), entries);
    //}

    @Override
    public ListSet<Entry<PopCount,T>> entrySet() {
        return entries;
    }

    @Override
    public Set<PopCount> keySet() {
        return keySet;
    }

    @Override
    public List<T> values() {
        return values;
    }

    @Override
    public T get(Object key) {
        return key instanceof PopCount ? get((PopCount) key) : null;
    }

    public T get(int pop) {
        return values.get(pop);
    }

    public T get(PopCount pop) {
        return values.get(pop.getIndex());
    }
}
