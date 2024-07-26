package mills.util.listset;

import mills.util.Indexed;

public class ArraySet<T extends Indexed> extends AbstractIndexedSet<T> implements IndexedListSet<T> {

    final T[] entries;

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

    public IndexedListSet<T> headSet(int size) {
        if(size==0)
            return new SingletonSet<>(get(0));

        return new HeadSet<>(entries, size);
    }

    public IndexedListSet<T> subSet(int offset, int size) {
        return new SubSet<>(entries, offset, size);
    }

    protected static class HeadSet<T extends Indexed> extends ArraySet<T> {

        final int size;

        public HeadSet(T[] entries, int size) {
            super(entries);
            this.size = size;

            if(size<0 || size>=entries.length)
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

    protected static class SubSet<T extends Indexed> extends HeadSet<T> {

        final int offset;

        SubSet(T[] entries, int offset, int size) {
            super(entries, size);
            this.offset = offset;
        }

        @Override
        public T get(int index) {
            inRange(index);
            return entries[offset+index] ;
        }
    }
}
