package mills.util;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.10.19
 * Time: 02:23
 */

abstract public class ArrayListSet<I> extends AbstractListSet<I> {

    protected final I[] values;

    public ArrayListSet(I[] values) {
        this.values = values;
    }

    @Override
    public I get(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public ListSet<I> subList(int fromIndex, int toIndex) {
        int size = checkRange(fromIndex, toIndex);
        return partition(fromIndex, size);
    }

    protected ListSet<I> partition(int fromIndex, int size) {
        return new SubList<>(values, fromIndex, size);
    }

    protected static class SubList<T> extends ArrayListSet<T> {

        final int offset;
        final int size;

        protected SubList(T[] values, int offset, int size) {
            super(values);
            this.offset = offset;
            this.size = size;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public T get(int index) {
            return super.get(index+offset);
        }

        @Override
        public ListSet<T> subList(int fromIndex, int toIndex) {
            return super.subList(fromIndex+offset, toIndex+offset);
        }
    }
}
