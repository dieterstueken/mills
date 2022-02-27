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
        return ListSet.of(values.subList(fromIndex, toIndex), comparator());
    }
}
