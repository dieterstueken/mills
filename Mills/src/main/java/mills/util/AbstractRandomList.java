package mills.util;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.12
 * Time: 18:23
 */
public abstract class AbstractRandomList<T> extends AbstractList<T> implements RandomAccess {

    public AbstractRandomList() {}

    abstract public int size();

    @Override
    abstract public T get(int index);

    public static <T> AbstractRandomList<T> of(T[] data) {
        return construct(data);
    }

    @SuppressWarnings("unchecked")
    private static <T> AbstractRandomList<T> construct(Object[] data) {

        return new AbstractRandomList<T>() {

            @Override
            public int size() {
                return data.length;
            }

            @Override
            public T get(int index) {
                return (T) data[index];
            }
        };
    }

    public static <T> List<T> virtual(int size, IntFunction<? extends T> generate) {
        return new AbstractRandomList<T>() {

            @Override
            public int size() {
                return size;
            }

            @Override
            public T get(int index) {
                return generate.apply(index);
            }
        };
    }

    public static <T> List<T> generate(int size, IntFunction<? extends T> generate) {

        Object values[] = new Object[size];
        for(int i=0; i<size; ++i)
            values[i] = generate.apply(i);

        return construct(values);
    }

    public static <U, T> List<T> map(List<U> source, Function<? super U, ? extends T> mapper) {
        int size = source.size();

        Object values[] = new Object[size];
        for(int i=0; i<size; ++i)
            values[i] = mapper.apply(source.get(i));

        return construct(values);
    }
}
