package mills.util;

import java.util.List;
import java.util.function.IntFunction;

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

    public static <T> AbstractRandomArray<T> of(int size, T value) {
        return new AbstractRandomArray<T>(size) {

            @Override
            public T get(int index) {
                return value;
            }
        };
    }

    public static <T> List<T> generate(int size, IntFunction<T> generate) {
        return new AbstractRandomArray<T>(size) {

            @Override
            public T get(int index) {
                return generate.apply(index);
            }
        }.immutableCopy();
    }
}
