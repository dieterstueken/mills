package mills.util.listset;

import mills.util.Indexed;

public class ArraySet<T extends Indexed> extends AbstractIndexedSet<T> implements IndexedListSet<T> {

    protected final T[] entries;

    protected ArraySet(T[] entries) {
        this.entries = entries;
    }

    public static <T extends Indexed> ArraySet<T> of(T[] entries) {

        if(!IndexedListSet.isOrdered(entries)) {
            throw new IllegalArgumentException("not ordered");
        }

        return new ArraySet<>(entries);
    }

    @Override
    public T get(int index) {
        return entries[index] ;
    }

    @Override
    public int size() {
        return entries.length;
    }

    @Override
    public int findIndex(T entry) {
        return super.findIndex(entry);
    }

    public IndexedListSet<T> headList(int size) {
        if(size==0)
            return new SingletonSet<>(get(0));

        return new HeadSet<>(entries, size);
    }

    @Override
    public IndexedListSet<T> subSet(int offset, int size) {
        return new SubSet<>(entries, offset, size);
    }

}
