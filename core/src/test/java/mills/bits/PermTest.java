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

        for (Perm first : Perm.VALUES) {
            for (Perm then : Perm.VALUES) {
                Perm pc = then.compose(first);

                assertEquals("Perm.compose", pc.ordinal(), then.compose(first.ordinal()));
                assertEquals("Perm.compose", pc.ordinal(), Perm.compose(then.ordinal(), first.ordinal()));

                System.out.format(" %s", pc);

                if(then.ordinal()==3)
                    System.out.print(" |");

                for (RingEntry e : Entries.TABLE) {
                    RingEntry ex = e.permute(first).permute(then);
                    RingEntry ey = e.permute(pc);
                    if(ex!=ey)
                        ey = e.permute(pc);
                    assertEquals("Perm.compose", ex, ey);
                }
            }
            System.out.println();

            if(first.ordinal()==3)
                System.out.println(" ------------------------+------------------------");
        }
    }
}
