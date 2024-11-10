package mills.util.listset;

import mills.util.AbstractRandomList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 10:32 AM
 */
abstract public class AbstractListSet<T> extends AbstractRandomList<T> implements ListSet<T> {

    @Override
    abstract public T get(int index);

    @Override
    abstract public int size();

    @Override
    public ListSet<T> subList(int fromIndex, int toIndex) {

        int size = checkRange(fromIndex, toIndex);

        if(size==size())
            return this;

        if(fromIndex==0)
            return headList(toIndex);

        return subSet(fromIndex, size);
    }

    abstract public ListSet<T> subSet(int offset, int size);

    public ListSet<T> headList(int size) {
        return subSet(0, size);
    }

    @Override
    public int lastIndexOf(final Object o) {
        // unique elements
        return indexOf(o);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        // not a collection or wrong size
        if (!(o instanceof Collection<?> c) || c.size()!=this.size())
            return false;

        // not a list, ask other one.
        if (!(o instanceof List<?> l))
            return o.equals(this);

        for(int i=0; i<size(); ++i) {
            if(!Objects.equals(get(i), l.get(i)))
                return false;
        }

        return true;
    }
}
