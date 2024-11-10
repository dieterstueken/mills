package mills.util.listset;

import mills.util.Indexed;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.11.24
 * Time: 21:38
 */
class SubSet<T extends Indexed> extends HeadSet<T> {

    final int offset;

    SubSet(T[] entries, int offset, int size) {
        super(entries, size);
        this.offset = offset;
    }

    @Override
    public T get(int index) {
        inRange(index);
        return entries[offset + index];
    }
}
