package mills.util;

import java.util.AbstractList;
import java.util.Comparator;

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

    abstract public ListSet<T> subList(int fromIndex, int toIndex);

    protected int checkRange(int fromIndex, int toIndex) {

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
        assert isOrdered(comparator()) : "index mismatch";
        return this;
    }

    protected boolean isOrdered(Comparator<? super T> order) {
        if(size()<2)
            return true;

        T t0 = get(0);
        for (int i = 1; i < size(); ++i) {
            T t1 = get(i);
            if(order.compare(t0, t1)>=0)
                return false;
            t0 = t1;
        }

        return  true;
    }
}
