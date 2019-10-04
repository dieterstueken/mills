package mills.util;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.10.19
 * Time: 19:04
 */
public class IndexedList<E extends Indexed> extends ListSetAdapter<E> implements IndexedSet<E> {

    public IndexedList(List<E> values) {
        super(values, Indexed.INDEXER);
        verify();
    }

    public IndexedList<E> subList(int fromIndex, int toIndex) {
        return new IndexedList<>(values.subList(fromIndex, toIndex));
    }
}
