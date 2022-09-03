package mills.util;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.08.22
 * Time: 19:24
 */
abstract public class DirectListSet<T> extends AbstractListSet<T> implements IndexedListSet<T> {
    @Override
    public int findIndex(final int index) {
        return inRange(index) ? index : -1;
    }

    static <T> DirectListSet<T> of(List<T> values, Indexer<? super T> comparator) {

        assert isDirect(values, comparator);

        return new DirectListSet<>() {
            @Override
            public T get(final int index) {
                return values.get(index);
            }

            @Override
            public int size() {
                return values.size();
            }

            @Override
            public ListSet<T> subList(final int fromIndex, final int toIndex) {
                return DelegateListSet.of(values.subList(fromIndex, toIndex), comparator());            }

            @Override
            public Indexer<? super T> comparator() {
                return comparator;
            }
        };
    }

    static <T> DirectListSet<T> of(T[] values, Indexer<? super T> comparator) {
        return new DirectListSet<>() {

            @Override
            public Indexer<? super T> comparator() {
                return comparator;
            }

            @Override
            public T get(final int index) {
                return values[index];
            }

            @Override
            public int size() {
                return values.length;
            }

            @Override
            public ListSet<T> subList(final int fromIndex, final int toIndex) {
                return DelegateListSet.of(List.of(values), comparator).subList(fromIndex, toIndex);
            }
        };
    }

    public static <T> boolean isDirect(List<T> values, Indexer<? super T> index) {
        for (int i = 0; i < values.size(); i++) {
            T value = values.get(i);
            if(index.indexOf(value)!=i)
                return false;
        }
        return true;
    }
}
