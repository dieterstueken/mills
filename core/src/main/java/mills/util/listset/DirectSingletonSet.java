package mills.util.listset;

import mills.util.Indexed;

class DirectSingletonSet<T extends Indexed> extends SingletonSet<T> implements DirectListSet<T> {

    public DirectSingletonSet(T value) {
        super(value);

        // to be a direct DirectSingletonSet requires: this.get(0) == value == 0
        if (value.getIndex()!=0)
            throw new IllegalArgumentException("Index not 0: " + value.getIndex());
    }

    public DirectListSet<T> headList(int size) {

        if (size == 0)
            return empty();

        if (size == 1)
            return this;

        throw new IllegalArgumentException("Size = " + size);
    }
}
