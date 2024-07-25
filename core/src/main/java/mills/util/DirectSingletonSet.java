package mills.util;

class DirectSingletonSet<T extends Indexed> extends SingletonSet<T> implements DirectListSet<T> {

    public DirectSingletonSet(T value) {
        super(value);

        if (!DirectListSet.isDirect(value))
            throw new IllegalArgumentException("Index not 0: " + value.getIndex());
    }

    public DirectListSet<T> headSet(int size) {

        if (size == 0)
            return empty();

        if (size == 1)
            return this;

        throw new IllegalArgumentException("Size = " + size);
    }
}
