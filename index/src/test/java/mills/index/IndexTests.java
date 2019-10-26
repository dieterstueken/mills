package mills.index;

import mills.bits.Perm;
import mills.bits.Perms;
import mills.bits.PopCount;
import mills.index.builder.IndexBuilder;
import mills.index.tables.C2Table;
import mills.position.Position;
import mills.position.Positions;
import mills.ring.Entries;
import mills.ring.EntryTables;
import mills.util.IntegerDigest;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 20.09.19
 * Time: 14:42
 */
public class IndexTests {

    EntryTables registry = new EntryTables();
    IndexBuilder builder = IndexBuilder.create(registry);

    /**
     * Iterate popcounts and execute a task on each.
     * @param tasks generator of tasks to execute.
     */
    void runTests(Function<PopCount, ForkJoinTask<Runnable>> tasks) {

        ForkJoinTask<Runnable> task = null;

        for (int nb = 0; nb < 10; ++nb) {
            for (int nw = 0; nw < 10; ++nw) {
                PopCount pop = PopCount.get(nb, nw);
                ForkJoinTask<Runnable> next = tasks.apply(pop).fork();

                if(task!=null)
                    task.join().run();
                task = next;
            }
        }

        task.join().run();
    }

    void indexTests(Consumer<PosIndex> test) {
        runTests(pop->indexTask(pop, test));
    }

    ForkJoinTask<Runnable> indexTask(PopCount pop, Consumer<PosIndex> test) {
        return new RecursiveTask<>() {
            @Override
            protected Runnable compute() {
                PosIndex posIndex = builder.build(pop);
                return () -> test.accept(posIndex);
            }
        };
    }

    @Test
    public void testDigest() {
        IntegerDigest digest = new IntegerDigest("MD5");

        System.out.format("start %d\n", Entries.TABLE.size());
        double start = System.currentTimeMillis();

        AtomicLong count20 = new AtomicLong();

        indexTests(posIndex->{
            PopCount pop = posIndex.pop();
            int range = posIndex.range();
            int n20 = posIndex.n20();
            count20.addAndGet(n20);
            System.out.format("l%d%d%,13d %4d\n", pop.nb, pop.nw, range, n20);
            digest.update(range);
        });

        double stop = System.currentTimeMillis();

        System.out.format("\n%.3fs, n20: %d, %,d\n", (stop - start) / 1000, count20.get(), Runtime.getRuntime().totalMemory());

        byte[] result = digest.digest();

        System.out.println("digest: " + IntegerDigest.toString(result));

        assertArrayEquals(IntegerDigest.EXPECTED, result);
    }

    @Test
    public void testPerms() {
        double start = System.currentTimeMillis();
        indexTests(this::testPerms);
        double stop = System.currentTimeMillis();
        System.out.format("\n%.3fs\n", (stop - start) / 1000);
    }

    private void testPerms(PosIndex index) {

        PopCount pop = index.pop();

        Perm.VALUES.parallelStream().forEach(perm->{
            index.process((idx, i201)->{
                long p201 = Positions.permute(i201, perm);
                long n201 = index.normalize(p201);
                if(!Positions.equals(n201, i201)) {
                    Position p0 = Position.of(i201);
                    Position pm = Position.of(n201);
                    Position pn = Position.of(n201);
                    n201 = index.normalize(p201);
                }
                assertTrue(Positions.equals(n201, i201));
            });
        });

        System.out.format("p(%d,%d) %,13d\n", pop.nb, pop.nw, index.range());
    }

    public C2Table build(PopCount pop, PopCount clop) {
        C2Table table = builder.build(pop, clop);
        int range = table.range();
        int n20 = table.n20();
        System.out.format("l%d%d%,13d %4d +%d\n", pop.nb, pop.nw, range, n20, registry.count());
        return table;
    }

    @Test
    public void testIndexGroups() {
        runGroupTests(this::indexGroup);
    }

    public void runGroupTests(BiConsumer<PopCount, Map<PopCount, C2Table>> test) {
        double start = System.currentTimeMillis();

        runTests(pop->groupTask(pop, test));

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

    private ForkJoinTask<Runnable> groupTask(PopCount pop, BiConsumer<PopCount, Map<PopCount, C2Table>> test) {
        return ForkJoinTask.adapt(()->groupAction(pop, test)).fork();
    }

    private Runnable groupAction(PopCount pop, BiConsumer<PopCount, Map<PopCount, C2Table>> test) {
        var group = builder.buildGroup(pop);
        return () -> test.accept(pop, group);
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

    @Test
    public void testNormalized() {
        runGroupTests((pop, tables) -> {
            tables.values().parallelStream().forEach(index->index.process((idx, i201) ->{
                Position pos = Position.of(i201);
                for (Perm perm : Perms.OTHER) {
                    Position ppos = pos.permute(perm);
                    if(ppos.i201 < pos.i201)
                        assert ppos.i201 >= pos.i201;
                }
            }));
        });
    }

    public void testIndex(PosIndex posIndex) {
        posIndex.process((idx, i201) ->{
            int kdx = posIndex.posIndex(i201);
            assertEquals(kdx, idx);
        });
    }

    @Test
    public void testIndex() {
        PopCount pop = PopCount.of(0,2);
        PosIndex posIndex = builder.build(pop);
        testIndex(posIndex);
    }
}
