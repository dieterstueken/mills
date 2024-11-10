package mills.util.listset;

import mills.util.Indexed;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.11.24
 * Time: 21:31
 */
class HeadSet<T extends Indexed> extends ArraySet<T> implements IndexedListSet<T> {

    final int size;

    public HeadSet(T[] entries, int size) {
        super(entries);
        this.size = size;

        if (size < 0 || size >= entries.length)
            throw new IllegalArgumentException("Size =" + size);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T get(int index) {
        checkIndex(index);
        return super.get(index);
    }
}
