package mills.util;

import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.09.2010
 * Time: 00:10:16
 */

/**
 * Class Indexer provides a mapping of elements <T> to int.
 * This enables Ordering operations on a List<E>.
 *
 * The alternative is a Function<E, Integer> which, however may create a lot of Integer objects.
 *
 * @param <T>
 */
public interface Indexer<T> extends Comparator<T> {

    Indexer<Indexed> INDEXED = Indexed::getIndex;
    Indexer<Enum<?>> ENUM = Enum::ordinal;

    // mapping to be implemented.
    int indexOf(T element);

    @Override
    default int compare(T e1, T e2) {
        if(e1==e2)
            return 0;

        if(e1==null)
            return -1;

        return Integer.compare(indexOf(e1), indexOf(e2));
    }

    default int lowerBound(List<? extends T> sortedList, int key) {
        int index = binarySearchKey(sortedList, key);
        return index<0 ? -1-index : index;
    }

    default int upperBound(List<? extends T> sortedList, int key) {
        int index = binarySearchKey(sortedList, key);
        return index<0 ? -1-index : index+1;
    }

    default int binarySearchKey(List<? extends T> sortedList, int key) {
        return binarySearchKey(sortedList, 0, sortedList.size(), key);
    }

    default int binarySearchKey(List<? extends T> sortedList, int fromIndex, int toIndex, int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = indexOf(sortedList.get(mid));

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
}
