package mills.util.listset;

import mills.util.Indexed;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.11.24
 * Time: 22:05
 */
class DirectHeadSet<T extends Indexed> extends HeadSet<T> implements DirectListSet<T> {

    public DirectHeadSet(T[] entries, int size) {
        super(entries, size);
    }

    public DirectListSet<T> headList(int size) {
        if (size == size())
            return this;

        if (size > size())
            throw new IllegalArgumentException("increasing size: " + size);

        return DirectArraySet.of(entries, size);
    }
}
