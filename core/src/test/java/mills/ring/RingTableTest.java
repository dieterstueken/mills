package mills.ring;

import mills.bits.BW;
import mills.bits.Pattern;
import mills.bits.Perm;
import mills.util.Stat;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/21/14
 * Time: 11:38 AM
 */
public class RingTableTest {

    @Test
    public void testTable() {
        for (RingEntry e : Entries.TABLE) {

            Pattern b = e.b;
            Pattern w = e.w;


            int index = BW.index(b, w);

            RingEntry f = Entries.of(index);

            if (e != f)
                fail(String.format("%d %d %d\n", index, e.index, f.index));
        }
    }

    @Test
    public void testSisters() {
        Stat stat = new Stat();
        Set<RingEntry> sisters = new TreeSet<>();

        for (RingEntry e : Entries.MINIMIZED) {

            for (Perm perm : Perm.VALUES) {
                RingEntry sister = e.permute(perm);
                sisters.add(sister);
            }

            int n = sisters.size();
            stat.accept(n);

            sisters.clear();
        }

        stat.dump("sisters");
    }
}
