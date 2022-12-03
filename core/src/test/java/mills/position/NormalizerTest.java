package mills.position;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
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
    static final ForkJoinPool pool = new ForkJoinPool();


    @Test
    void normalizerTest() {
        long start = System.currentTimeMillis();
        for(int i2=0; i2<MAX_INDEX; ++i2) {
            short s2 = index(i2);
            pool.invoke(ForkJoinTask.adapt(() -> testI201(s2)));
            long stop = System.currentTimeMillis();
            System.out.format("%.1f: %d\n", (stop-start)/1000.0, i2);
        }
    }

    private void testI201(short s2) {
        IntStream.range(0, MAX_INDEX).parallel()
                .forEach(i0 -> {
                    short s0 = index(i0);
                    IntStream.range(0, MAX_INDEX).parallel()
                            .forEach(i1 -> testI201(s2, s0, index(i1)));
                });
    }

    private void testI201(short i2, short i0, short i1) {
        long i201 = Positions.i201(i2, i0, i1);
        long m201 = Positions.NORMALIZER.build(i201);

        assertOp(i201, m201, Normalizer.NORMAL::build);
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