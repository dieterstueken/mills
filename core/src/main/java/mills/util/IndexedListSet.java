package mills.util;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.08.22
 * Time: 14:09
 */
public interface IndexedListSet<T> extends ListSet<T> {

    @Override
    Indexer<? super T> comparator();

    default int findIndex(int key) {
        return comparator().binarySearchKey(this, key);
    }

    default int indexOf(Object o) {
        return o instanceof Indexed idx ? findIndex(idx.getIndex()) : -1;
    }

    static <T> IndexedDelegate<T> of(List<T> values, Indexer<? super T> comparator) {
        return new IndexedDelegate<>(values) {

            @Override
            public Indexer<? super T> comparator() {
                return comparator;
            }
        };
    }

    static <T extends Indexed> IndexedDelegate<T> of(List<T> values) {
        return new IndexedDelegate<>(values) {

            @Override
            public Indexer<? super T> comparator() {
                return Indexer.INDEXED;
            }
        };
    }

    static <T extends Indexed> IndexedListSet<T> ifDirect(List<T> values) {
        if(DirectListSet.isDirect(values, Indexer.INDEXED))
            return DirectListSet.of(values, Indexer.INDEXED);
        else
            return IndexedListSet.of(values);
    }
}
