package mills.ring;

import mills.bits.BW;
import mills.bits.Pattern;
import org.junit.Test;

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
        for (RingEntry e : Entry.TABLE) {

            Pattern b = e.b;
            Pattern w = e.w;


            int index = BW.index(b, w);

            RingEntry f = Entry.of(index);

            if (e != f)
                fail(String.format("%d %d %d\n", index, e.index, f.index));
        }
    }
}
