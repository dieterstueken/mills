package mills.position;

import org.junit.jupiter.api.Test;

import java.util.List;

import static mills.position.Swap.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  29.11.2022 18:18
 * modified by: $
 * modified on: $
 */
class SwapTest {

    public static final List<Swap> VALUES = List.of(T201, T012, T120, S021, S102, S210);

    static final long I201 = 0x0002000000010000L;

    @Test
    void invert() {
        for (Swap t : VALUES) {
            long i201 = t.build(I201);
            Swap inv = t.invert();
            long j201 = inv.build(i201);
            System.out.format("%s=%s %016x %016x\n", t, inv, i201, j201);
            if(j201!=I201) {
                j201 = inv.build(i201);
            }
            assertEquals(j201, I201, "for: " + t);
        }
    }

    @Test
    void compose() {
        System.out.append("    ");
        for (Swap t0 : VALUES) {
            if(t0.getIndex()==1) System.out.append(' ');
            System.out.append(" ").append(t0.toString());
        }
        System.out.println();

        for (Swap t0 : VALUES) {
            System.out.append(t0.toString());
            long i0 = t0.build(I201);
            for (Swap t1 : VALUES) {
                Swap t2 = t0.compose(t1);
                long i1 = t1.build(i0);
                long i2 = t2.build(I201);
                if(t1.getIndex()==1) System.out.append(' ');
                System.out.format("  %d%d%d", Positions.i2(i1), Positions.i0(i1), Positions.i1(i1));
                //System.out.append("  ").append(t2.toString());

                if(!Positions.equals(i1,i2)) {
                    System.err.println();
                    System.err.format(" %d%d%d.%x/%d%d%d.%x\n",
                            Positions.i2(i1), Positions.i0(i1), Positions.i1(i1), Positions.stat(i1),
                            Positions.i2(i2), Positions.i0(i2), Positions.i1(i2), Positions.stat(i2));
                }

                assertTrue(Positions.equals(i1,i2));
            }
            System.out.println();
        }
    }
}