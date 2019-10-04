package mills.util;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  17.09.2019 11:35
 * modified by: $
 * modified on: $
 */
public interface Indexed extends Comparable<Indexed> {

    int getIndex();

    @Override
    default int compareTo(Indexed o) {
        return Integer.compare(getIndex(), o.getIndex());
    }

}
