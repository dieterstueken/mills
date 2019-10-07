package mills.index2;

import mills.bits.Perms;
import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.index1.IndexList;
import mills.position.Position;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.IntegerDigest;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 20.09.19
 * Time: 14:42
 */
public class IndexTest {

    IndexBuilder builder = IndexBuilder.create();
    IndexList refBuilder = IndexList.create();

    @Test
    public void testVerifyOld() {
        testVerifyOld(PopCount.get(0,1));
    }

    public void testVerifyOld(PopCount pop) {
        // compare positions of new and old builders

        PosIndex pi = builder.build(pop);
        PosIndex ri = refBuilder.get(pop);

        System.out.format("l%d%d%10d, %4d : %10d,%4d\n\n",
                pop.nb, pop.nw,
                pi.range(), pi.n20(),
                ri.range(), ri.n20());

        Map<Integer, Long> missing = new TreeMap<>();
        Map<Integer, Long> duplicates = new TreeMap<>();

        ri.process(missing::put);

        System.out.println("start");

        pi.process((index, p201)-> {
            Position pos = Position.of(p201);
            int refIndex = ri.posIndex(p201);
            //System.out.format("%s: %d %d\n", pos, index, refIndex);

            if(missing.remove(refIndex)==null)
                duplicates.put(refIndex, p201);
        });

        System.out.format("missing: %d\n", missing.size());

        missing.entrySet().forEach(e->System.out.format("%s: %d\n", Position.of(e.getValue()), e.getKey()));

        System.out.format("duplicates: %d\n", duplicates.size());

        duplicates.entrySet().forEach(e->System.out.format("%s: %d\n", Position.of(e.getValue()), e.getKey()));

        System.out.println();

        List<Position> positions = missing.values().stream().map(Position::of).collect(Collectors.toList());

        assert missing.size()==0;
        assert duplicates.size()==0;
    }

    @Test
    public void testDigest() {
        IntegerDigest digest = new IntegerDigest("MD5");

        System.out.format("start %d\n", Entries.TABLE.size());

        PosIndex[] indexes = new PosIndex[PopCount.SIZE];
        Arrays.parallelSetAll(indexes, i -> builder.build(PopCount.TABLE.get(i)));

        double start = System.currentTimeMillis();
        long count20 = 0;

        for (int nb = 0; nb < 10; ++nb) {
            for (int nw = 0; nw < 10; ++nw) {
                PopCount pop = PopCount.of(nb, nw);
                PosIndex posIndex = indexes[pop.index];
                int range = posIndex.range();
                int n20 = posIndex.n20();
                count20 += n20;
                System.out.format("l%d%d%10d, %4d\n", pop.nb, pop.nw, range, n20);
                digest.update(range);
            }
        }
            
        double stop = System.currentTimeMillis();
        System.out.format("%.3fs, n20: %d\n", (stop - start) / 1000, count20);

        byte[] result = digest.digest();

        System.out.println("digest: " + IntegerDigest.toString(result));

        assertArrayEquals(IntegerDigest.EXPECTED, result);
    }

    @Test
    public void testFragments() {
        // find if fragments are unique for each PopCount

        PosIndex[] indexes = new PosIndex[PopCount.TABLE.size()];

        PopCount.TABLE.parallelStream().map(builder::build)
                .forEach(index ->indexes[index.pop().index] = index);

        System.out.println("start");

        Set<Perms> perms = new TreeSet<>();

        EntryTables stat = new EntryTables();

        PopCount.TABLE.stream().filter(pop->pop.sum()<=8).forEach(pop -> {
            EntryTable[] fragment = builder.fragments.get(pop.index);

            Set<EntryTable> tables = new TreeSet<>(Entries.BY_SIZE);
            int n=0;
            for (int m = 0; m < fragment.length; m++) {
                EntryTable table = fragment[m];
                if (table != null) {
                    Perms perm = Perms.of(2*m);
                    perms.add(perm);
                    ++n;
                    tables.add(table);
                    stat.table(table);
                }
            }

            System.out.format("%s: %d %d\n", pop, n, tables.size());
        });
        
        perms.forEach(System.out::println);

        stat.stat(System.out);

        System.out.format("\n total: %d\n", stat.count());
    }
}
