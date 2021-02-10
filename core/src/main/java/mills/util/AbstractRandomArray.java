package mills.util;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.12.12
 * Time: 11:58
 */
public abstract class AbstractRandomArray<T> extends AbstractRandomList<T> {

    protected final int size;

    public AbstractRandomArray(int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    public static <T> AbstractRandomArray<T> virtual(T[] data) {
        return _asList(data);
    }

    static <T> AbstractRandomArray<T> _asList(Object[] data) {

        return new AbstractRandomArray<T>(data.length) {

            @Override
            @SuppressWarnings("unchecked")
            public T get(int index) {
                return (T) data[index];
            }

            @Override
            @SuppressWarnings("unchecked")
            public T set(int index, T value) {
                T t = (T) data[index];
                if(t==value)
                    return t;

                data[index] = value;
                modCount = 0;
                return t;
            }

            @Override
            public int hashCode() {
                if(modCount==0) {
                    this.modCount = Arrays.hashCode(data);
                    if(this.modCount==0)
                        this.modCount=1;
                }
                return modCount;
            }
        };
    }
}
