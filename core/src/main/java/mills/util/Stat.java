package mills.util;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.11.2015 10:48
 * modified by: $Author$
 * modified on: $Date$
 */
public class Stat implements IntConsumer {

    final ConcurrentSkipListMap<Integer, AtomicInteger> stat = new ConcurrentSkipListMap<>();

    public int get(int index) {
        AtomicInteger value = stat.get(index);
        return value==null ? 0 : value.get();
    }

    public int max() {
        Integer value = stat.floorKey(Integer.MAX_VALUE);
        return value==null ? 0 : value;
    }

    @Override
    public void accept(int value) {
        stat.computeIfAbsent(value, i -> new AtomicInteger()).incrementAndGet();
    }

    public void dump(String title) {

        System.out.format("%s: %d %d\n", title, stat.size(), sum());

        for (Map.Entry<Integer, AtomicInteger> e : stat.entrySet()) {
            System.out.format("%4x %4d\n", e.getKey(), e.getValue().get());
        }
    }

    public void forEach(BiConsumer<Integer, Integer> action) {
        stat.forEach((k,v) -> action.accept(k, v.get()));
    }

    public int sum() {
        int sum = 0;
        for(AtomicInteger v : stat.values())
            sum += v.get();

        return sum;
    }

    public Stat process(Stream<? extends Collection<?>> ts) {
        ts.mapToInt(Collection::size).forEach(this);
        return this;
    }

    public String toString() {
        return String.format("Hist[%d]", stat.size());
    }
}
