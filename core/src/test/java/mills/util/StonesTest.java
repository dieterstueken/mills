package mills.util;

import mills.stones.Stones;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.08.23
 * Time: 21:23
 */
public class StonesTest {

    final AtomicInteger count = new AtomicInteger(0);

    @Test
    public void testCloses() {
        IntStream.range(0, 1<<24).parallel().forEach(this::testCloses);
        System.out.format("%x\n", count.get());
    }

    void testCloses(int stones) {
        int closes = Stones.closes(stones);
        if(closes!=0) {
            int closed = Stones.closed(stones);
            for (int m = Stones.free(stones), j = m & -m; j != 0; m ^= j, j = m & -m) {
                int stones1 = stones | j;
                int closed1 = Stones.closed(stones1);
                assertEquals(closed1 == closed, (closes & j) == 0);
            }
            count.incrementAndGet();
        }
    }
}
