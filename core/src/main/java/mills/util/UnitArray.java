package mills.util;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.10.19
 * Time: 20:40
 */
public class UnitArray<T extends Indexed> extends IndexedArray<T> implements UnitSet<T> {

    protected UnitArray(T[] values) {
        super(values);
        verify();
    }

    public IndexedSet<T> subList(int fromIndex, int toIndex) {
        if(fromIndex==0)
            return subSet(toIndex);

        int size = checkRange(fromIndex, toIndex);
        return partition(fromIndex, size);
    }

    @Override
    public UnitSet<T> subSet(int size) {
        if(size==this.size())
            return this;

        checkRange(0, size);

        return new UnitArray<>(values) {
            @Override
            public int size() {
                return size;
            }
        };
    }

    protected UnitArray<T> verify() {
        assert verify(values)==null;
        return this;
    }

    static <T extends Indexed> T verify(T[] values) {

        for(int i=0; i<values.length; ++i) {
            T value = values[i];
            if(value.getIndex()!=i)
                return value;
        }

        return null;
    }
}
