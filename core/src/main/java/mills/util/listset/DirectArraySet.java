package mills.util.listset;

import mills.bits.Perms;
import mills.util.Indexed;

public class DirectArraySet<T extends Indexed> extends ArraySet<T> implements DirectListSet<T> {

    protected DirectArraySet(T[] entries) {
        super(entries);
    }

    static <T extends Indexed> DirectListSet<T> of(T[] entries, int size) {

        if(size==0 || entries==null)
            return empty();

        if(size<0 || size>entries.length)
            throw new IllegalArgumentException("Size = " + size);

        if(size==1)
            return new DirectSingletonSet<>(entries[0]);

        if(size==entries.length)
            return new DirectArraySet<>(entries);

        return new DirectHeadSet<>(entries, size);
    }

    public DirectListSet<T> headList(int size) {
        if(size == size())
            return this;

        if(size>size())
            throw new IllegalArgumentException("increasing size: " + size);

        return of(entries, size);
    }

}
