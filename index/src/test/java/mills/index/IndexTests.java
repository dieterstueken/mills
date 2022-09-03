package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.builder.IndexGroup;
import mills.index.builder.IndexGroups;
import mills.util.CachedBuilder;
import org.junit.jupiter.api.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * version:     IndexCompare$
 * created by:  d.stueken
 * created on:  02.03.2021 19:53
 * modified by: $
 * modified on: $
 */
public class IndexTests {


    static final ReferenceQueue<IndexGroup> queue = new ReferenceQueue<>();

    static class GroupReference extends SoftReference<IndexGroup> {

        final PopCount pop;

        public GroupReference(IndexGroup referent) {
            super(referent, queue);
            pop = referent.pop();
        }

        @Override
        public String toString() {
            return "Ref(" + pop + ')';
        }
    }

    final IndexGroups.Debug DEBUG = new IndexGroups.Debug() {
        @Override
        public void start(final PopCount pop) {
            System.err.format("%s start\n", pop);
        }

        @Override
        public void done(final IndexGroup result) {
            System.err.format("%s done\n", result.pop());
        }

        @Override
        public Reference<IndexGroup> newReference(IndexGroup value) {
            return new GroupReference(value);
        }
    };

    final IndexGroups groups;

    final Thread queueRunner;

    {
        groups = new IndexGroups(DEBUG);

        queueRunner = new Thread("queue runner") {
            @Override
            public void run() {
                try {
                while(!Thread.currentThread().isInterrupted()) {
                    Reference<?> ref = queue.remove();
                    System.err.println("enqueued: " + ref.toString());
                }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        queueRunner.start();

    }

    Thread parallelBuild() {

        return new Thread() {

            final RandomGenerator rg = RandomGenerator.getDefault();
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    int n = rg.nextInt(100);
                    PopCount pop = PopCount.get(n);
                    groups.build(pop);
                }
            }
        };
    }

    public Runnable compute(PopCount pop) {
        return () -> groups.group(pop);
    }

    public Stream<IndexGroup> groups() {
        return PopCount.TABLE.stream().map(groups::group);
    }

    @Test
    public void buildParallelGroups() throws InterruptedException {
        // prefetch
         //List<? extends ForkJoinTask<?>> tasks = PopCount.TABLE
         //        .subList(50,100)
         //        .transform(e -> ForkJoinTask.adapt(compute(e)))
         //        .copyOf();
         //
         //ForkJoinTask.adapt(()->ForkJoinTask.invokeAll(tasks)).fork();

        Thread parallelBuild = parallelBuild();
        parallelBuild.start();

        buildGroups();

        parallelBuild.interrupt();
        parallelBuild.join();
    }

    @Test
    public void buildGroups() {

        timer("time", () -> {
            long total = 0;

            for (IndexGroups.Provider provider : groups.providers()) {
                boolean exists = provider.cached() != null;
                IndexGroup group = provider.get();

                total += group.range();

                System.out.format("%s %sgroups: %d\n",
                        group.pop(),
                        exists ? "ready " : "",
                        group.group().size());

                group.group().forEach((clop, c2t) -> {
                    System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
                });
            }

            System.out.format("total: %,d\n", total);

            return null;
        });

        System.out.format("memory: %dMb\n", Runtime.getRuntime().totalMemory()/1024/1024);
        groups.providers().forEach(CachedBuilder::clear);
        Runtime.getRuntime().gc();
        System.out.format("memory: %dMb\n", Runtime.getRuntime().totalMemory()/1024/1024);
    }

    public void createOOM() {

        System.err.format("max memory: %,d\n", Runtime.getRuntime().maxMemory());

        List<Integer> dummy = List.of(42,43, 44);
        while(true) {
            List<Integer> tmp = new ArrayList<>(dummy);
            tmp.addAll(dummy);
            dummy=tmp;
            System.err.format("size: %,d\n", tmp.size());
        }
    }

    @Test
    public void testIndexes() {
        groups().forEach(this::testIndexes);
    }

    void testIndexes(PosIndex index) {
        System.out.format("testIndexes %s\n", Clops.of(index));

        IntStream.range(0, index.range()).parallel()
                .forEach(i->testIndex(index, i));
    }

    void testIndex(PosIndex ix, int pi0) {
        long i201 = ix.i201(pi0);
        int pi1 = ix.posIndex(i201);
        assertEquals(pi1, pi0);
    }

    static <T> T timer(String name, Supplier<T> proc) {
        double start = System.currentTimeMillis();
        T t = proc.get();
        double stop = System.currentTimeMillis();

        System.out.format("%s: %.3fs\n", name, (stop - start) / 1000);

        return t;
    }
}
