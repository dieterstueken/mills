package mills.index2;

import mills.ring.RingEntry;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/25/14
 * Time: 11:13 AM
 */
public interface RingMap<T> {

    int size();

    default int range() {
        int i = size();
        return i==0 ? 0 : index(i-1);
    }

    default boolean isEmpty() {
        return size()==0;
    }

    int index(int i);

    RingEntry entry(int i);

    T get(int i);
}
