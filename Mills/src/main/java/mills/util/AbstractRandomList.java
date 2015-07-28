package mills.util;

import java.util.AbstractList;
import java.util.Collections;
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

    private static <T> AbstractRandomList<T> construct(T[] data) {

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

    public static <U, T> List<T> virtual(List<U> source, Function<? super U, ? extends T> mapper) {
         return new AbstractRandomList<T>() {

             @Override
             public int size() {
                 return source.size();
             }

             @Override
             public T get(int index) {
                 return mapper.apply(source.get(index));
             }
         };
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> generate(int size, IntFunction<? extends T> generate) {

        if(size==0)
            return Collections.emptyList();

        if(size==1)
            return Collections.singletonList(generate.apply(0));

        Object values[] = new Object[size];
        for(int i=0; i<size; ++i)
            values[i] = generate.apply(i);

        return construct((T[]) values);
    }

    @SuppressWarnings("unchecked")
    public static <U, T> List<T> map(List<U> source, Function<? super U, ? extends T> mapper) {
        int size = source.size();

        if(size==0)
            return Collections.emptyList();

        if(size==1)
            return Collections.singletonList(mapper.apply(source.get(0)));

        Object values[] = new Object[size];
        for(int i=0; i<size; ++i)
            values[i] = mapper.apply(source.get(i));

        return construct((T[])values);
    }
}
