package mills.bits;

import mills.ring.Entries;
import mills.ring.RingEntry;
import mills.util.Indexer;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.11.24
 * Time: 16:21
 */
class PermsTest {

    @Test
    public void showPerms()
    {
        Perms.VALUES.forEach(System.out::println);
    }

    @Test
    public void meqPerms() {
        Set<Perms> perms = new TreeSet<>(Indexer.INDEXED);

        Entries.TABLE.stream().mapToInt(RingEntry::pmeq).mapToObj(Perms::of).forEach(perms::add);

        System.out.format("%d:\n", perms.size());
        for (Perms perm : perms) {
            System.out.println(perm);
        }
    }
}