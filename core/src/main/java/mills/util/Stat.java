package mills.util;

import mills.ring.EntryTable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.11.2015 10:48
 * modified by: $Author$
 * modified on: $Date$
 */
public class Stat implements IntConsumer {

    final Map<Integer, AtomicInteger> stat = new ConcurrentSkipListMap<>();

    @Override
    public void accept(int value) {
        stat.computeIfAbsent(value, i -> new AtomicInteger()).incrementAndGet();
    }

    public void dump(String title) {
        System.out.println(title);
        for (Map.Entry<Integer, AtomicInteger> e : stat.entrySet()) {
            System.out.format("%4d %4d\n", e.getKey(), e.getValue().get());
        }
    }

    public Stat process(IntStream is) {
        is.forEach(this);
        return this;
    }

    public Stat process(Stream<EntryTable> ts) {
        return process(ts.mapToInt(List::size));
    }

    public String toString() {
        return String.format("Hist[%d]", stat.size());
    }
}
