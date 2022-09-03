package mills.ring;

import mills.bits.BW;
import mills.bits.Pattern;
import mills.bits.Perm;
import mills.position.Positions;
import mills.util.Stat;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.fail;

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

    @Test
    public void testMeq() {

        for (RingEntry e2 : Entries.TABLE) {
            for (RingEntry e0 : Entries.TABLE) {

                final int i20 = Positions.i20(e2, e0);
                int meq = 0;

                for (int pm = 0; pm < 8; ++pm) {
                    RingEntry p2 = e2.permute(pm);
                    RingEntry p0 = e0.permute(pm);

                    int p20 = Positions.i20(p2, p0);
                    int p02 = Positions.i20(p0, p2);

                    if(p20<i20 || p02<i20) {
                        meq = 0;
                        break;
                    }

                    if(i20==p20 | i20==p02)
                        meq |= 1<<pm;
                }

                int meq2 = Positions.meq(e2, e0);
                if(meq != meq2)
                    fail();
            }
        }
    }

    @Test
    public void testStat() {
        Set<Integer> meq = new TreeSet<>();
        Set<Integer> mlt = new TreeSet<>();

        for (RingEntry e : Entries.TABLE) {
            mlt.add(e.pmlt());
            meq.add(e.pmeq());
        }

        System.out.format("meq: %d, mlt: %d\n", meq.size(), mlt.size());
    }

    @Test
    public void testX() {

        Entries.EMPTY.index();

        final int MAX=500;

        long result = 0;
        long count=0;

        System.out.println("start");
        double start = System.currentTimeMillis();
        for (RingEntry e2 : Entries.MINIMIZED) {
            for (RingEntry e0 : Entries.TABLE.tailSet(e2)) {
                ++count;
                for(int i=0; i<MAX; ++i)
                    result += Positions.meq(e2, e0);
            }
        }
        double stop = System.currentTimeMillis();
        System.out.format("%,d: %,1f/ms\n", count, count * MAX / (stop - start));
    }

    @Test
    public void testMinRad() {
        List<RingEntry> mirad = new ArrayList<>();

        for (RingEntry e : Entries.RADIALS) {
            if(e.isMin())
                mirad.add(e);
        }

        mirad.forEach(e -> System.out.format("%s %d\n", e, e.sisters().size()));
    }

}
