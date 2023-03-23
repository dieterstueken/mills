package mills;

import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  23.03.2023 14:18
 * modified by: $
 * modified on: $
 */
public class StreamTest {

    @Test
    public void testStream() {
        IntStream s1 = IntStream.range(0,100);
        IntStream s2 = IntStream.range(100,200);

        IntStream s3 = IntStream.concat(s1, s2);

        int sum = s3.parallel().map(this::peek).sum();
        System.out.println(sum);

        results.forEach((t,b) -> System.out.format("%s: %d\n", t.getName(), b.size()));
    }

    final Map<Thread, BitSet> results = new ConcurrentHashMap<>();

    int peek(int i) {
        Thread thread = Thread.currentThread();
        results.computeIfAbsent(thread, t -> new BitSet()).set(i);
        return i;
    }
}
