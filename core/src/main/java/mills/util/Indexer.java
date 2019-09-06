package mills.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

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
abstract public class Indexer<T> implements Comparator<T>, ToIntFunction<T> {

    // mapping to be implemented.
    abstract public int index(T element);

    @Override
    public int applyAsInt(T value) {
        return index(value);
    }

    public int compare(T e1, T e2) {
        return index(e1) - index(e2);
    }

    public int lowerBound(List<? extends T> sortedList, int key) {
        int index = binarySearchKey(sortedList, key);
        return index<0 ? -1-index : index;
    }

    public int upperBound(List<? extends T> sortedList, int key) {
        int index = binarySearchKey(sortedList, key);
        return index<0 ? -1-index : index+1;
    }

    public int binarySearchKey(List<? extends T> sortedList, int key) {
        return binarySearchKey(sortedList, 0, sortedList.size(), key);
    }

    public int binarySearchKey(List<? extends T> sortedList, int fromIndex, int toIndex, int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = index(sortedList.get(mid));

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