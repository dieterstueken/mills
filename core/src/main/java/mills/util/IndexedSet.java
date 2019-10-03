package mills.util;

import java.util.List;

/**
 * Class IndexedSet implements a sorted set of indexes values.
 * @param <I> the type of values.
 */
abstract public class IndexedSet<I extends Indexed> extends ListSet<I> {

    public static <I extends Indexed> IndexedSet<I> of(I[] values) {

        return new IndexedSet<I>() {

            @Override
            public I get(int index) {
                return values[index];
            }

            @Override
            public int size() {
                return values.length;
            }
        }.verify();
    }

    public static <I extends Indexed> IndexedSet<I> of(List<I> values) {

        return new IndexedSet<I>() {

            @Override
            public I get(int index) {
                return values.get(index);
            }

            @Override
            public int size() {
                return values.size();
            }
        }.verify();
    }

    @Override
    public int indexOf(Object o) {
        if(o instanceof Indexed) {
            int key = ((Indexed)o).getIndex();
            return Indexed.INDEXER.binarySearchKey(this, key);
        } else
            return super.indexOf(o);
    };

    @Override
    public Indexer<? super I> comparator() {
        return Indexed.INDEXER;
    }

    IndexedSet<I> verify() {
        super.verify();
        return this;
    }

}
