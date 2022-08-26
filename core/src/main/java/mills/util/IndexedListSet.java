package mills.util;

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
}
