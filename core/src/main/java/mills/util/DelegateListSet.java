package mills.util;

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
        return of(values.subList(fromIndex, toIndex), comparator);
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

    public static <T> ListSet<T> of(List<T> values, Comparator<? super T> comparator) {
        assert isOrdered(values, comparator) : "index mismatch";
        return new DelegateListSet<>(values, comparator);
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

    public static <T extends Indexed> ListSet<T> ofDirect(List<T> values, Comparator<? super T> comparator) {

        assert isDirect(values, comparator) : "index mismatch";

        return new DelegateListSet<>(values, comparator) {

        };
    }

    static <T extends Indexed> boolean isDirect(List<T> values, Comparator<? super T> order) {
        for (int i = 0; i < values.size(); i++) {
            T value = values.get(i);
            if(value.getIndex()!=i)
                return false;
        }
        return true;
    }
}
