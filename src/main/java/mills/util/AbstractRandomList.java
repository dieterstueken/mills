package mills.util;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
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

        return new AbstractRandomList<T>() {

            @Override
            public int size() {
                return data.length;
            }

            @Override
            public T get(int index) {
                return data[index];
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

        return new AbstractRandomList<T>() {

            @Override
            public int size() {
                return size;
            }

            // The fake cast to E is safe since the generate method returned a T
            @Override
            @SuppressWarnings("unchecked")
            public T get(int index) {
                return (T) values[index];
            }
        };
    }
}
