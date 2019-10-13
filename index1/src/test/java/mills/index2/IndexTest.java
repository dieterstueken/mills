package mills.index2;

import mills.bits.Perms;
import mills.bits.PopCount;
import mills.bits.RClop;
import mills.index.PosIndex;
import mills.index1.C2Table;
import mills.index1.IndexList;
import mills.position.Position;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.IntegerDigest;
import mills.util.Stat;
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

    EntryTables registry = new EntryTables();
    IndexBuilder builder = IndexBuilder.create(registry);
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
        double start = System.currentTimeMillis();

        PosIndex[] indexes = new PosIndex[PopCount.SIZE];
        Arrays.parallelSetAll(indexes, i -> builder.build(PopCount.TABLE.get(i)));

        double stop = System.currentTimeMillis();
        
        long count20 = 0;

        for (int nb = 0; nb < 10; ++nb) {
            for (int nw = 0; nw < 10; ++nw) {
                PopCount pop = PopCount.of(nb, nw);
                PosIndex posIndex = indexes[pop.index];
                int range = posIndex.range();
                int n20 = posIndex.n20();
                count20 += n20;
                System.out.format("l%d%d%,13d %4d\n", pop.nb, pop.nw, range, n20);
                digest.update(range);
            }
        }
            
        System.out.format("\n%.3fs, n20: %d, %,d\n", (stop - start) / 1000, count20, Runtime.getRuntime().totalMemory());

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

        PopCount debug = PopCount.of(3,4);

        long k =PopCount.TABLE.stream().filter(pop->pop.sum()<=8).mapToInt(pop -> {
            EntryTable[] fragment = builder.fragments.get(pop.index);

            Set<EntryTable> tset = new TreeSet<>(Entries.BY_SIZE);
            int n=0;
            if(pop.equals(debug))
                n=0;

            for (int m = 0; m < fragment.length; m++) {
                EntryTable table = fragment[m];
                if (table != null) {
                    Perms perm = Perms.of(2*m);
                    perms.add(perm);
                    ++n;
                    tset.add(table);
                }
            }

            Stat stat = new Stat();

            tset.forEach(et -> stat.accept(countClops(et)));
            stat.forEach((i,j)-> System.out.format(" %d[%d]", i, j));
            System.out.println();

            System.out.format("%s: %d %d, ", pop, n, tset.size());
            return tset.size();
        }).sum();

        System.out.format("\n tset: %d, total: %d\n", k, registry.count());

        perms.forEach(System.out::println);

        registry.stat(System.out);
    }

    private int countClops(EntryTable et) {

        Set<RClop> rset = new TreeSet<>();
        et.forEach(e->rset.add(RClop.of(e)));

        Map<RClop, Set<RClop>> rmap = new TreeMap<>();

        rset.forEach(rc -> {
            rc.rad.forEachMinor(rx->{
                PopCount cx = rc.clop.add(rx.pop);
                if(cx.max()<=4) {
                    RClop rcx = RClop.of(rx, rc.clop.add(rx.pop));
                    rmap.computeIfAbsent(rcx, x -> new TreeSet<>()).add(rc);
                }
            });
        });

        Set<Set<RClop>> xset = new HashSet<>(rmap.values());

        return xset.size();
    }

    @Test
    public void indexGroups() {
        double start = System.currentTimeMillis();

        for (int nb = 0; nb <= 9; ++nb) {
            for (int nw = 0; nw <= 9; ++nw) {
                PopCount pop = PopCount.get(nb, nw);
                indexGroup(pop);
            }
        }

        double stop = System.currentTimeMillis();

        System.out.format("\n%.3fs, mem: %,d\n", (stop - start) / 1000, Runtime.getRuntime().totalMemory());

        registry.stat(System.out);
    }

    @Test
    public void indexGroups1() {
        for (int nb = 0; nb <= 9; ++nb) {
            PopCount pop = PopCount.get(nb, nb);
            indexGroup(pop);
        }
    }

    @Test
    public void testGroup() {
        PopCount pop = PopCount.get(5, 5);
        Map<PopCount, C2Table> group = indexGroup(pop);

        PosIndex ref = refBuilder.get(pop);
        BitSet refSet = new BitSet(ref.range());
        refSet.set(0, ref.range());

        group.values().stream().forEach(pi->
            pi.process((posIndex, i201)->{
                int refIndex = ref.posIndex(i201);
                refSet.set(refIndex, false);
            })
        );

        var positions = refSet.stream().mapToLong(ref::i201).mapToObj(Position::of).collect(Collectors.toList());

        System.out.format("missing: %d\n", positions.size());
    }

    public Map<PopCount, C2Table> indexGroup(PopCount pop) {
        var group = builder.buildGroup(pop);

        PopCount max = group.keySet().stream().reduce(PopCount.EMPTY, PopCount::max);

        int count = group.values().stream().mapToInt(PosIndex::range).sum();

        System.out.format("group (%d,%d) [%d,%d] +%d: %,d\n",
                pop.nb, pop.nw, max.nb, max.nw,
                group.size(), count);

        for (int mb = 0; mb <= max.nb; ++mb) {
            for (int mw = 0; mw <= max.nw; ++mw) {
                PopCount clop = PopCount.of(mb, mw);
                PosIndex pi = group.get(clop);
                if(pi==null)
                    System.out.append("             |");
                else
                    System.out.format("%,13d|", pi.range());
            }
            System.out.println();
        }

        System.out.println();

        return group;
    }
}
