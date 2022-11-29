package mills.bits;

import org.junit.jupiter.api.Test;

import java.util.List;

import static mills.bits.Swapped.T0;
import static mills.bits.Swapped.T1;
import static mills.bits.Swapped.T2;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  29.11.2022 18:18
 * modified by: $
 * modified on: $
 */
class SwapTest {

    public static final List<Twist> VALUES = List.of(T0, T1, T2, T0.S, T1.S, T2.S);

    static final long I201 = 0x0003000200010000L;

    @Test
    void invert() {
        for (Twist t : VALUES) {
            Twist inv = t.invert();
            long i201 = t.twist(I201);
            long j201 = inv.twist(i201);
            System.out.format("%s=%s %16x %16x\n", t, inv, i201, j201);
            assertEquals(j201, I201, "for: " + t);
        }
    }

    @Test
    void compose() {
        System.out.append("      ");
        for (Twist t0 : VALUES) {
            if(t0.msk==1) System.out.append(' ');
            System.out.append(' ').append(t0.name);
        }
        System.out.println();

        for (Twist t0 : VALUES) {
            System.out.append(t0.name).append(' ');
            long i0 = t0.twist(I201);
            for (Twist t1 : VALUES) {
                Twist t2 = t0.compose(t1);
                long i1 = t1.twist(i0);
                long i2 = t2.twist(I201);
                if(t1.msk==1) System.out.append(' ');
                System.out.append(' ').append(t2.name);
                assertEquals(i1, i2);
            }
            System.out.println();
        }
    }
}