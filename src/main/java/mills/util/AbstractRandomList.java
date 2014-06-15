package mills.util;

import com.google.common.collect.ImmutableList;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.12
 * Time: 18:23
 */
public abstract class AbstractRandomList<T> extends AbstractList<T> implements RandomAccess {

    public AbstractRandomList() {}

    abstract public int size();

    public List<T> immutableCopy() {
        return ImmutableList.copyOf(this);
    }

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
}
