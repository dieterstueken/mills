package mills.index;

import mills.bits.PopCount;
import mills.bits.RClop;
import mills.index.builder.IndexBuilder;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.IntegerDigest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 20.09.19
 * Time: 14:42
 */
public class IndexTests {

    EntryTables registry = new EntryTables();
    IndexBuilder builder = IndexBuilder.create(registry);

    @Test
    public void testDigest() {
        IntegerDigest digest = new IntegerDigest("MD5");

        System.out.format("start %d\n", Entries.TABLE.size());
        double start = System.currentTimeMillis();

        AtomicLong count20 = new AtomicLong();

        ForkJoinTask<Runnable> task = null;

        for (int nb = 0; nb < 10; ++nb) {
            for (int nw = 0; nw < 10; ++nw) {
                PopCount pop = PopCount.get(nb, nw);

                ForkJoinTask<Runnable> next = new RecursiveTask<>() {
                    @Override
                    protected Runnable compute() {
                        PosIndex posIndex = builder.build(pop);
                        return () -> {
                            int range = posIndex.range();
                            int n20 = posIndex.n20();
                            count20.addAndGet(n20);
                            System.out.format("l%d%d%,13d %4d\n", pop.nb, pop.nw, range, n20);
                            digest.update(range);
                        };
                    }
                };

                next.fork();

                if(task!=null)
                    task.join().run();

                task = next;
            }
        }

        task.join().run();

        double stop = System.currentTimeMillis();

        System.out.format("\n%.3fs, n20: %d, %,d\n", (stop - start) / 1000, count20.get(), Runtime.getRuntime().totalMemory());

        byte[] result = digest.digest();

        System.out.println("digest: " + IntegerDigest.toString(result));

        assertArrayEquals(IntegerDigest.EXPECTED, result);
    }

    public C2Table build(PopCount pop, PopCount clop) {
        C2Table table = builder.build(pop, clop);
        int range = table.range();
        int n20 = table.n20();
        System.out.format("l%d%d%,13d %4d +%d\n", pop.nb, pop.nw, range, n20, registry.count());
        return table;
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

        ForkJoinTask<Runnable> task = null;

        for (int nb = 0; nb <= 9; ++nb) {
            for (int nw = 0; nw <= 9; ++nw) {
                PopCount pop = PopCount.get(nb, nw);

                ForkJoinTask<Runnable> next = groupTask(pop);

                if(task!=null)
                    task.join().run();
                task = next;
            }
        }

        task.join().run();

        double stop = System.currentTimeMillis();

        System.out.format("\n%.3fs, mem: %,d\n", (stop - start) / 1000, Runtime.getRuntime().totalMemory());

        registry.stat(System.out);
    }

    @Test
    public void indexGroups1() {
        double start = System.currentTimeMillis();

        for (int nb = 0; nb <= 9; ++nb) {
            PopCount pop = PopCount.get(nb, nb);
            indexGroup(pop);
        }
        
        double stop = System.currentTimeMillis();

        System.out.format("\n%.3fs, mem: %,d\n", (stop - start) / 1000, Runtime.getRuntime().totalMemory());
    }

    @Test
    public void indexGroups0() {

        double start = System.currentTimeMillis();

        for (int nb = 0; nb <= 9; ++nb) {
            PopCount pop = PopCount.get(nb, nb);
            build(pop, PopCount.EMPTY);
        }

        double stop = System.currentTimeMillis();

        System.out.format("\n%.3fs, mem: %,d\n", (stop - start) / 1000, Runtime.getRuntime().totalMemory());
    }

    private ForkJoinTask<Runnable> groupTask(PopCount pop) {
        return ForkJoinTask.adapt(()->groupAction(pop)).fork();
    }

    private Runnable groupAction(PopCount pop) {
        var group = builder.buildGroup(pop);
        return () -> indexGroup(pop, group);
    }

    public Map<PopCount, C2Table> indexGroup(PopCount pop) {
        return indexGroup(pop, builder.buildGroup(pop));
    }

    public Map<PopCount, C2Table> indexGroup(PopCount pop, Map<PopCount, C2Table> group) {

        PopCount max = group.keySet().stream().reduce(PopCount.EMPTY, PopCount::max);

        int count = group.values().stream().mapToInt(PosIndex::range).sum();

        System.out.format("group (%d,%d) [%d,%d] +%d: %,d +%d\n",
                pop.nb, pop.nw, max.nb, max.nw,
                group.size(), count, registry.count());

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
