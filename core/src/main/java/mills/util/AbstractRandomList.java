package mills.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

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

    public static <T> List<T> virtual(int size, IntFunction<? extends T> generate) {
        return new AbstractRandomArray<T>(size) {

            @Override
            public T get(int index) {
                return generate.apply(index);
            }

            // no content equal for virtual lists
            @Override
            public boolean equals(Object o) {
                return o == this;
            }

            @Override
            public int hashCode() {
                return System.identityHashCode(this);
            }
        };
    }

    public static <U, T> List<T> transform(List<U> source, Function<? super U, ? extends T> mapper) {
         return new AbstractRandomList<T>() {

             @Override
             public int size() {
                 return source.size();
             }

             @Override
             public T get(int index) {
                 return mapper.apply(source.get(index));
             }

             // no content equals for virtual lists
             @Override
             public boolean equals(Object o) {
                 return o == this;
             }

             @Override
             public int hashCode() {
                 return System.identityHashCode(this);
             }

             @Override
             public T remove(int index) {
                 U removed = source.remove(index);
                 return removed == null ? null : mapper.apply(removed);
             }

             public void clear() {
                 source.clear();
             }

             public Stream<T> stream() {
                 return source.stream().map(mapper);
             }
         };
    }

    public static <T> List<T> generate(int size, IntFunction<? extends T> generate) {

        if(size==0)
            return Collections.emptyList();

        if(size==1)
            return Collections.singletonList(generate.apply(0));

        Object values[] = new Object[size];
        for(int i=0; i<size; ++i)
            values[i] = generate.apply(i);

        return AbstractRandomArray.construct(values);
    }

    @SuppressWarnings("unchecked")
    public static <U, T> List<T> map(List<U> source, Function<? super U, ? extends T> mapper) {
        int size = source.size();

        if(size==0)
            return Collections.emptyList();

        if(size==1)
            return Collections.singletonList(mapper.apply(source.get(0)));

        // back to forth
        Object values[] = new Object[size];
        for(int i=size-1; i>=0; --i)
            values[i] = mapper.apply(source.get(i));

        return AbstractRandomArray.construct(values);
    }

    @SuppressWarnings("unchecked")
    public static <U, T> List<T> collect(Collection<U> source, Function<? super U, ? extends T> mapper) {
        int size = source.size();

        if(size==0)
            return Collections.emptyList();

        if(size==1)
            return Collections.singletonList(mapper.apply(source.iterator().next()));

        Object values[] = new Object[size];
        int i=0;
        for (U v : source) {
            values[i++] = v;
        }

        return AbstractRandomArray.construct(values);
    }

    // check sizes first
    // fast RandomAccess compare

    @Override
    public boolean equals(Object o) {
        return o instanceof List && equals((List) o);
    }

    public boolean equals(List<?> other) {

        if(this==other)
            return true;

        int size = size();

        if(size!=other.size()) {
            return false;
        } else {
            if(other instanceof RandomAccess) {
                for (int i = 0; i < size; ++i) {
                    if (!Objects.equals(get(i), other.get(i)))
                        return false;
                }
            } else
                return super.equals(other);
        }

        return true;
    }
}
