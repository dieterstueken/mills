package mills.util;

import mills.util.listset.ListSet;

import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Function;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  26.08.2022 13:12
 * modified by: $
 * modified on: $
 */
public interface RandomList<T> extends List<T>, RandomAccess {

    default <E> RandomList<E> transform(Function<? super T, ? extends E> mapper) {
        return AbstractRandomList.transform(this, mapper);
    }

    default ListSet<T> asListSet(Comparator<? super T> comparator) {
        return ListSet.of(this, comparator);
    }

    default List<T> copyOf() {
        return List.copyOf(this);
    }

    static <T> boolean isOrdered(List<T> values, Comparator<? super T> order) {

        if(values.size()<2)
            return true;

        T t0 = values.getFirst();
        for (int i = 1; i < values.size(); ++i) {
            T t1 = values.get(i);
            if(order.compare(t0, t1)>=0)
                return false;
            t0 = t1;
        }

        return  true;
    }
}
