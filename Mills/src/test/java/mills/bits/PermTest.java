package mills.bits;

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
        for (RingEntry e : RingEntry.TABLE) {

            for (Perm p : Perm.VALUES) {
                RingEntry ep = e.permute(p);
                RingEntry ex = ep.permute(p.inverse());
                assertEquals("Perm.inverse", e, ex);
            }
        }
    }

    @Test
    public void compose() {

        for (Perm p1 : Perm.VALUES) {
            for (Perm p2 : Perm.VALUES) {
                Perm pc = p1.compose(p2);
                for (RingEntry e : RingEntry.TABLE) {
                    RingEntry ex = e.permute(p1).permute(p2);
                    RingEntry ey = e.permute(pc);
                    assertEquals("Perm.compose", ex, ey);
                }
            }
        }
    }
}
