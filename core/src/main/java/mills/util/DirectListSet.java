package mills.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.08.22
 * Time: 14:09
 */
public class DirectListSet<T> extends DelegateListSet<T> {

    final Indexer<? super T> indexer;

    protected DirectListSet(List<T> values, Indexer<? super T> indexer) {
        super(values);
        this.indexer = indexer;
    }

    @Override
    public Comparator<? super T> comparator() {
        return indexer;
    }

    @Override
    public int findIndex(T entry) {
        int index = indexer.indexOf(entry);

        // turn indexes beyond size into a negative value.
        if(index>=values.size())
            index = -index-1;

        return index;
    }


    public static <T> DirectListSet<T> of(List<? extends T> values, Indexer<? super T> indexer) {

        assert isDirect(values, indexer);

        return new DirectListSet<>(List.copyOf(values), indexer);
    }

    static <T extends Indexed> ListSet<T> of(IntFunction<? extends T> generator, int size) {
        List<? extends T> values = IntStream.of(size).mapToObj(generator).toList();
        return new DirectListSet<>(List.copyOf(values), Indexer.INDEXED);
    }

    static <E extends Enum<E>> ListSet<E> of(Class<E> type) {
        List<E> values = List.of(type.getEnumConstants());
        return new DirectListSet<>(values, Indexer.ENUM) {
            @Override
            public int indexOf(final Object o) {
                if(type.isInstance(o))
                    return type.cast(o).ordinal();
                else
                    return -1;
            }
        };
    }

    static <I extends Indexed> ListSet<I> of(I ... values) {
        List<I> indexed = List.of(values);

        assert isDirect(indexed, Indexer.INDEXED);

        return new DirectListSet<>(indexed, Indexer.INDEXED) {

            @Override
            public int indexOf(final Object o) {
                return o instanceof Indexed indexed ? indexed.getIndex() : -1;
            }
        };
    }

    static <T> boolean isDirect(List<T> values, Indexer<? super T> indexer) {

        for (final T value : values) {
            if(indexer.indexOf(value)==0)
                return false;
        }

        return true;
    }

}
