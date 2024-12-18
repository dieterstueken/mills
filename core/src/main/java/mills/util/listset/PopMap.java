package mills.util.listset;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 10:16
 */
public class PopMap<T> extends ListMap<PopCount, T> {

    protected PopMap(ListSet<PopCount> keys, List<T> values) {
        super(keys, values);
    }

    public static <T> PopMap<T> of(ListSet<PopCount> keys, List<T> values) {
        if(keys instanceof DirectListSet<PopCount> dls)
            return new DirectPopMap<>(dls, values);
        else
            return new PopMap<>(keys, values);
    }

    public static <T> PopMap<T> of(PopCount key, T value) {
        return new PopMap<>(AbstractIndexedSet.singleton(key), List.of(value)) {
            @Override
            public T get(PopCount pop) {
                return key.equals(pop) ? value : null;
            }

            @Override
            public T getOf(int index) {
                return index==0 ? value : null;
            }
        };
    }

    public static <T> PopMap<T> ofDirect(DirectListSet<PopCount> keys, List<T> values) {
        return new DirectPopMap<>(keys, values);
    }

    static <T> PopMap<T> of(List<T> values) {
        return ofDirect(PopCount.TABLE, values);
    }

    /**
     * In contrast to ListMap the values determine the size which may be smaller than 100
     * @return the size of this map
     */
    @Override
    public int size() {
        return keySet.size();
    }

    public T get(PopCount pop) {
        return pop==null ? null : super.get(pop);
    }

    public T getOf(int index) {
        return get(PopCount.get(index));
    }

    public int findIndex(PopCount pop) {
        return keySet.findIndex(pop);
    }

    public T put(PopCount pop, T value) {
        int index = findIndex(pop);
        return values.set(index, value);
    }

    public static <T> PopMap<T> allocate(int size) {
        var table = AbstractRandomList.<T>preset(size, null);
        return PopMap.of(table);
    }

    public static PopMap<EntryTable> lePops(EntryTable root) {
        return PopMap.of(PopCount.TABLE.transform(pop -> root.filter(pop.le)));
    }

    public static <T> PopMap<T> allocate() {
        return allocate(PopCount.SIZE);
    }

    public void dumpInt(String head, Function<T, Integer> toInt) {
        System.out.println(head);
        PopCount.OF.map(this::get)
                .map(toInt, null)
                .map("%5d"::formatted, "     ")
                .dump(9, "%5d");
    }
}
