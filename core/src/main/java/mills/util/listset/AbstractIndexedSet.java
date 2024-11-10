package mills.util.listset;

import mills.util.Indexed;

import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 10:32 AM
 */
abstract public class AbstractIndexedSet<T extends Indexed> extends AbstractListSet<T> implements IndexedListSet<T> {

    @Override
    abstract public T get(int index);

    @Override
    abstract public int size();

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof List<?> l))
            return false;

        if (l.size() != size())
            return false;

        for(int i=0; i<size(); ++i) {
            if(!Objects.equals(get(i), l.get(i)))
                return false;
        }

        return true;
    }

    private static final EmptyListSet<Indexed> EMPTY = new EmptyListSet<>();

    @SuppressWarnings("unchecked")
    public static <T extends Indexed> DirectListSet<T> empty() {
        return (EmptyListSet<T>) EMPTY;
    }

    public static <T extends Indexed> IndexedListSet<T> singleton(T value) {
        return new SingletonSet<>(value);
    }
}
