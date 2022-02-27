package mills.util;

import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.08.22
 * Time: 13:38
 */
class OrderedListSet<T> extends DelegateListSet<T> {

    protected final Comparator<? super T> comparator;

    protected OrderedListSet(List<T> values, Comparator<? super T> comparator) {
        super(values);
        this.comparator = comparator;
    }
    
    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public boolean add(T t) {
        int index = findIndex(t);

        // already contained
        if (index >= 0)
            return false;

        values.add(-index - 1, t);

        return true;
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public boolean remove(Object o) {
        int index = findIndex((T)o);
        if(index<0)
            return false;

        values.remove(index);

        return true;
    }

    @Override
    public T remove(int index) {
        return values.remove(index);
    }

    public static <T> OrderedListSet<T> of(List<T> values, Comparator<? super T> comparator) {

        assert isOrdered(values, comparator) : "index mismatch";

        return new OrderedListSet<>(values, comparator);
    }
}
