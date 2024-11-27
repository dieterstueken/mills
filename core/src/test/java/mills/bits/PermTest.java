package mills.bits;

import mills.position.Positions;
import mills.ring.Entries;
import mills.ring.RingEntry;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
                assertEquals(e, ex, "Perm.inverse");
            }
        }
    }

    @Test
    public void composeTest() {

        for (Perm first : Perm.VALUES) {
            for (Perm then : Perm.VALUES) {
                Perm pc = then.compose(first);

                assertEquals(pc.ordinal(), then.compose(first.ordinal()), "Perm.compose");
                assertEquals(pc.ordinal(), Positions.compose(then.perm(), first.perm()), "Perm.compose");

                System.out.format(" %s", pc);

                if(then.ordinal()==3)
                    System.out.print(" |");

                for (RingEntry e : Entries.TABLE) {
                    RingEntry ex = e.permute(first).permute(then);
                    RingEntry ey = e.permute(pc);
                    if(ex!=ey)
                        ey = e.permute(pc);
                    assertEquals(ex, ey, "Perm.compose");
                }
            }
            System.out.println();

            if(first.ordinal()==3)
                System.out.println(" ------------------------+------------------------");
        }
    }

    @Test
    public void testMapping() {
        Perm.VALUES.forEach(this::testPerm);
    }

    private void testPerm(Perm perm) {
        SectMap m0 = SectMap.of(perm);
        SectMap mp = sectMap(perm);
        assertEquals(m0, mp);

        SectMap mi = m0.invert();
        mp = sectMap(perm.invert());
        assertEquals(mi, mp);

        for (Perm px : Perm.VALUES) {
            SectMap mc = m0.composed(px);
            mp = sectMap(perm.compose(px));
            assertEquals(mc, mp);
        }
    }

    static SectMap sectMap(Perm perm) {
        Function<Sector, Sector> map = Function.identity();

        int rot = perm.rotates();
        for(int i=0; i<rot; ++i)
            map = map.andThen(Sector::rotate);

        if(perm.mirrors())
            map = map.andThen(Sector::mirror);

        return SectMap.of(map);
    }

    @Test
    public void mlt2() {
        Set<Perms> permset = Entries.TABLE.stream().flatMap(e2 ->
                        Entries.TABLE.tailSet(e2).stream()
                                .map(e0 -> Perms.of(Positions.mlt20(e2, e0))))
                .collect(Collectors.toSet());

        System.out.format("%d:\n", permset.size());
        permset.forEach(System.out::println);
    }
}
