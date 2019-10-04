package mills.util;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 10:32 AM
 */
abstract public class AbstractListSet<T> extends AbstractList<T> implements ListSet<T> {

    @Override
    abstract public T get(int index);

    @Override
    abstract public int size();

    @Override
    abstract public ListSet<T> subList(int fromIndex, int toIndex);

    public int checkRange(int fromIndex, int toIndex) {

        if(fromIndex<0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);

        if (toIndex > size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);

        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");

        return toIndex-fromIndex;
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

    public static <T> ListSet<T> of(List<T> values, Comparator<? super T> comparator) {

        return new AbstractListSet<>() {
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
            public Comparator<? super T> comparator() {
                return comparator;
            }
        };
    }
}
