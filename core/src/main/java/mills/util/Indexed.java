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

    static <T extends Indexed> T min(T a, T b) {
        return a.getIndex()<b.getIndex() ? a : b;
    }

    static <T extends Indexed> T max(T a, T b) {
        return a.getIndex()>b.getIndex() ? a : b;
    }
}
