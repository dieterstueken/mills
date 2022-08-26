package mills.util;

import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.08.22
 * Time: 15:25
 */
abstract public class DelegateListSet<T> extends AbstractListSet<T>  {

    protected final List<T> values;

    public DelegateListSet(final List<T> values) {
        this.values = values;
    }

    @Override
    public T get(int index) {
        return values.get(index);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public ListSet<T> subList(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if(fromIndex==toIndex)
            return empty();

        if(fromIndex==toIndex+1)
            return singleton(get(fromIndex));

        return of(values.subList(fromIndex, toIndex), comparator());
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

    protected AbstractListSet<T> verify() {
        assert isOrdered(this, comparator()) : "index mismatch";
        return this;
    }

    static <T> boolean isOrdered(List<T> values, Comparator<? super T> order) {

        if(values.size()<2)
            return true;

        T t0 = values.get(0);
        for (int i = 1; i < values.size(); ++i) {
            T t1 = values.get(i);
            if(order.compare(t0, t1)>=0)
                return false;
            t0 = t1;
        }

        return  true;
    }

    static <T> DelegateListSet<T> of(List<T> values, Comparator<? super T> comparator) {
        return new DelegateListSet<>(values) {
            @Override
            public Comparator<? super T> comparator() {
                return comparator;
            }
        };
    }
}
