package mills.util.listset;

import mills.bits.PopCount;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.24
 * Time: 17:11
 */
public class DirectPopMap<T> extends PopMap<T> {

    protected DirectPopMap(final DirectListSet<PopCount> keys, final List<T> values) {
        super(keys, values);
    }

    @Override
    public T get(PopCount pop) {
        return getValue(pop.index);
    }

    @Override
    public T getOf(int index) {
        return getValue(index);
    }
}
