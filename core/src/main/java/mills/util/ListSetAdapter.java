package mills.util;

import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.10.19
 * Time: 02:57
 */
public class ListSetAdapter<E> extends AbstractListSet<E> {

    protected final List<E> values;

    final Comparator<? super E> comparator;

    protected ListSetAdapter(List<E> values, Comparator<? super E> comparator) {
        this.values = values;
        this.comparator = comparator;
        verify();
    }

    @Override
    public E get(int index) {
        return values.get(index);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public ListSet<E> subList(int fromIndex, int toIndex) {
        return new ListSetAdapter<>(values.subList(fromIndex, toIndex), comparator);
    }

}
