package mills.position;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongUnaryOperator;
import java.util.stream.IntStream;

import static mills.ring.RingEntry.MAX_INDEX;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  03.12.2022 18:54
 * modified by: $
 * modified on: $
 */
class NormalizerTest {

    static final long PRIME = 1009;

    static short index(int index) {
        long m = index*PRIME;
        return (short) (m%MAX_INDEX);
    }

    long start=System.currentTimeMillis();
    final AtomicInteger count = new AtomicInteger();

    volatile double limit;
    final double INCREMENT = Math.pow(3, 1/3.0);

    @Test
    void normalizerTest() {
        count.set(0);
        limit = 3;
        long start = System.currentTimeMillis();

        IntStream.range(0, MAX_INDEX).parallel().forEach(this::testI201);

        long stop = System.currentTimeMillis();
        System.out.format("%4d: %.1f\n", count.get(), (stop - start) / 1000.0);
    }


    private void testI201(int i2) {
        short s2 = index(i2);
        for (int i0 = 0; i0 < MAX_INDEX; ++i0) {
            short s0 = index(i0);
            for (int i1 = 0; i1 < MAX_INDEX; ++i1) {
                short s1 = index(i0);
                testI201(s2, s0, s1);
            }
        }
        
        int i = count.incrementAndGet();

        if(i>limit+0.5) {
            limit *= INCREMENT;
            long stop = System.currentTimeMillis();
            System.out.format("%4d: %.1f\n", i-1, (stop - start) / 1000.0);
        }
    }

    private void testI201(short i2, short i0, short i1) {
        long i201 = Positions.i201(i2, i0, i1);
        //long m201 = Positions.NORMALIZER.build(i201);
        long m201 = Normalizer.NORMAL.build(i201);

        //assertOp(i201, m201, Normalizer.NORMAL::build);
        //assertOp(m201, i201, Positions::revert);
    }

    static void assertOp(long i201, long r201, LongUnaryOperator op) {
        long m201 = op.applyAsLong(i201);

        if(!Positions.equals(m201,r201)) {
            System.err.format("(%s)->(%s) != (%s)\n",
                    Positions.format(i201),
                    Positions.format(m201),
                    Positions.format(r201));

                    op.applyAsLong(i201);
        }

        assertTrue(Positions.equals(m201,r201));
    }
}