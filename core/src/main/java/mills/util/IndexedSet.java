package mills.util;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Class IndexedSet implements a sorted set of indexes values.
 * @param <I> the type of values.
 */
public interface IndexedSet<I extends Indexed> extends ListSet<I> {

    @Override
    default int indexOf(Object o) {
        if(o instanceof Indexed) {
            int key = ((Indexed)o).getIndex();
            return Indexed.INDEXER.binarySearchKey(this, key);
        } else
            return -1;
    };

    @Override
    default Comparator<? super I> comparator() {
        return Indexed.INDEXER;
    }

    default <T extends Indexed> IndexedSet<T> transform(Function<? super I, ? extends T> mapper) {
        return new IndexedList<>(AbstractRandomList.transform(this, mapper));
    }
}
