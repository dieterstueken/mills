package mills.util;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.10.19
 * Time: 19:04
 */
public class IndexedArray<I extends Indexed> extends ArrayListSet<I> implements IndexedSet<I> {

    protected IndexedArray(I[] values) {
        super(values);
        verify();
    }

    @Override
    public I get(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    protected static class SubIndex<T extends Indexed> extends ArrayListSet.SubList<T> implements IndexedSet<T> {

        protected SubIndex(T[] values, int offset, int size) {
            super(values, offset, size);
        }

        @Override
        public IndexedSet<T> subList(int fromIndex, int toIndex) {
            int size = checkRange(fromIndex, toIndex);
            return new SubIndex<T>(values, offset+fromIndex, size);
        }
    }

    protected IndexedSet<I> partition(int fromIndex, int size) {
        return new SubIndex<I>(values, fromIndex, size);
    }
}
