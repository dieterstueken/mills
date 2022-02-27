package mills.util;

import mills.bits.PopCount;

import java.util.List;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 10:16
 */
public class PopMap<T> extends ListMap<PopCount, T> {

    public PopMap(ListSet<PopCount> keys, List<T> values) {
        super(keys, values);
    }

    public PopMap(List<T> values) {
        super(PopCount.TABLE, values);
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
        if(pop==null)
            return null;
        else
            return super.getValue(pop.index);
    }

    public T put(PopCount pop, T value) {
        return values.set(pop.index, value);
    }


    public static <T> PopMap<T> allocate(int size) {
        var table = AbstractRandomList.<T>preset(size, null);
        return new PopMap<>(table);
    }

    public static <T> PopMap<T> allocate() {
        return allocate(PopCount.SIZE);
    }

    public void dump(String head, Function<T, String> dump) {

        System.out.println(head);

        for (int nb = 0; nb < 9; nb++) {
            for (int nw = 0; nw < 9; nw++) {
                PopCount pop = PopCount.of(nb, nw);
                T pt = get(pop);
                System.out.print(dump.apply(pt));
            }

            System.out.println();
        }

        System.out.println();
    }
}
