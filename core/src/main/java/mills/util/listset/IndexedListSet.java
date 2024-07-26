package mills.util.listset;

import mills.util.Indexed;
import mills.util.Indexer;
import mills.util.RandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.08.22
 * Time: 14:09
 */
public interface IndexedListSet<T extends Indexed> extends ListSet<T> {

    @Override
    default Indexer<? super T> comparator() {
        return Indexer.INDEXED;
    }

    default int findIndex(int key) {
        return Indexer.INDEXED.binarySearchKey(this, key);
    }

    default int indexOf(Object o) {
        return o instanceof Indexed idx ? findIndex(idx.getIndex()) : -1;
    }

    static <T extends Indexed> boolean isOrdered(List<T> values) {
         return RandomList.isOrdered(values, Indexer.INDEXED);
    }

    static <T extends Indexed> boolean isOrdered(T[] values) {
        return ListSet.isOrdered(values, Indexer.INDEXED);
    }
}
