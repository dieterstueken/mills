package mills.util.listset;

import mills.util.Indexed;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;

class EmptyListSet<T extends Indexed> extends AbstractListSet<T> implements DirectListSet<T> {

    @Override
    public T get(int index) {
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public EmptyListSet<T> subSet(int fromIndex, int size) {
        checkIndex(fromIndex);

        if(size!=0)
            throw new IllegalArgumentException("Size = " + size);

        return this;
    }

    @Override
    public EmptyListSet<T> headSet(int toIndex) {
        if(toIndex!=0)
            throw new IllegalArgumentException("Size = " + toIndex);

        return this;
    }

    @Override
    public Stream<T> stream() {
        return Stream.of();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
    }

    @Override
    public int hashCode() {
        return Collections.emptyList().hashCode();
    }
}
