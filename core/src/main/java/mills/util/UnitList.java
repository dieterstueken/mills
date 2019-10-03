package mills.util;

/**
 * Class UnitList represents a root table of indexed entries with get(i).getIndex()==i.
 * @param <I> the type of entries.
 */
abstract public class UnitList<I extends Indexed> extends IndexedSet<I> {

    public static <I extends Indexed> UnitList<I> root(I[] values) {

        return new UnitList<>() {

            @Override
            public I get(int index) {
                return values[index];
            }

            @Override
            public int size() {
                return values.length;
            }
        };
    }

    @Override
    public int findIndex(I key) {
        return key.getIndex();
    }

    UnitList<I> verify() {
        for(int i=0; i<size(); ++i) {
            assert i == get(i).getIndex();
        }

        return this;
    }
}