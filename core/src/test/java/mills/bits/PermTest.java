package mills.bits;

import mills.ring.Entries;
import mills.ring.RingEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 5/10/15
 * Time: 7:36 PM
 */
public class PermTest {

    @Test
    public void testInv() {
        for (Perm perm : Perm.VALUES) {
            Perm inv = perm.invert();
            System.out.format("%s -> %s\n", perm, inv);

            for (RingEntry e : Entries.TABLE) {
                RingEntry ep = e.permute(perm);
                RingEntry ex = ep.permute(inv);
                assertEquals("Perm.inverse", e, ex);
            }
        }
    }

    @Test
    public void composeTest() {

        for (Perm p1 : Perm.VALUES) {
            for (Perm p2 : Perm.VALUES) {
                Perm pc = p1.compose(p2);
                assertEquals("Perm.compose", pc.ordinal(), p1.compose(p2.ordinal()));
                assertEquals("Perm.compose", pc.ordinal(), Perm.compose(p1.ordinal(), p2.ordinal()));

                for (RingEntry e : Entries.TABLE) {
                    RingEntry ex = e.permute(p1).permute(p2);
                    RingEntry ey = e.permute(pc);
                    assertEquals("Perm.compose", ex, ey);
                }
                System.out.format(" %s", pc);
                if(p2.ordinal()==3)
                    System.out.print(" |");
            }
            System.out.println();

            if(p1.ordinal()==3)
                System.out.println(" ------------------------+------------------------");
        }
    }
}
