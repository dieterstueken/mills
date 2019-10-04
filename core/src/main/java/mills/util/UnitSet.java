package mills.util;

/**
 * Class UnitList represents a root table of indexed entries with get(i).getIndex()==i.
 * @param <I> the type of entries.
 */
public interface UnitSet<I extends Indexed> extends IndexedSet<I> {

    static <T extends Indexed> UnitArray<T> of(T[] values) {
        return new UnitArray<>(values);
    }

    @Override
    default int findIndex(I key) {
        return key.getIndex();
    }

    UnitSet<I> subSet(int size);
}