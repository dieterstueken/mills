package mills.util.listset;

import mills.util.Indexed;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SingletonSet<T extends Indexed> extends AbstractIndexedSet<T> {

    final T value;

    public SingletonSet(T value) {
        this.value = value;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int indexOf(Object obj) {
        return Objects.equals(obj, value) ? 0 : -1;
    }

    @Override
    public IndexedListSet<T> subSet(int fromIndex, int size) {

        if (fromIndex == 0)
            return headSet(size);

        throw new IllegalArgumentException("Size = " + size);
    }

    @Override
    public IndexedListSet<T> headSet(int size) {

        if (size == 0)
            return empty();

        if (size == 1)
            return this;

        throw new IllegalArgumentException("Size = " + size);
    }

    @Override
    public T get(int index) {
        if (index == 0)
            return value;
        else
            return AbstractIndexedSet.<T>empty().get(index);
    }

    @Override
    public T getFirst() {
        return value;
    }

    @Override
    public Iterator<T> iterator() {
        //return Iterators.singletonIterator(entry());
        return super.iterator();
    }

    @Override
    public Stream<T> stream() {
        return Stream.of(value);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        action.accept(value);
    }

    @Override
    public int hashCode() {
        return 31 + value.hashCode();
    }
}
