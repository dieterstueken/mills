package mills.index.builder;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 10:16
 */
public class PopMap<T> {

    final List<T> table;

    public PopMap(List<T> table) {
        this.table = table;
    }

    public T get(PopCount pop) {
        if(pop==null)
            return null;

        if(pop.index< table.size())
            return table.get(pop.index);

        return defaultValue();
    }

    public T put(PopCount pop, T value) {
        return table.set(pop.index, value);
    }

    T defaultValue() {
        return null;
    }

    public static PopMap<EntryTable> lePops(EntryTable root) {
        return new PopMap<>(AbstractRandomList.transform(PopCount.TABLE, pop -> root.filter(pop.le)));
    }

    public static <T> PopMap<T> allocate(int size) {
        var table = AbstractRandomList.<T>preset(size, null);
        return new PopMap<>(table);
    }

    public static <T> PopMap<T> allocate() {
        return allocate(PopCount.SIZE);
    }
}
