package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.builder.GroupBuilder;
import mills.index.builder.IndexBuilder;
import mills.index.builder.IndexGroup;
import mills.util.CachedBuilder;
import org.junit.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

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

    final GroupBuilder.Debug DEBUG = new GroupBuilder.Debug() {
        @Override
        public void start(final PopCount pop) {
            System.out.format("%s start\n", pop);
        }

        @Override
        public void done(final IndexGroup result) {
            System.out.format("%s done\n", result.pop());
        }

        @Override
        public Reference<IndexGroup> newReference(IndexGroup value) {
            return new GroupReference(value);
        }
    };

    final GroupBuilder groupBuilder;

    final Thread queueRunner;

    {
        groupBuilder = new GroupBuilder(DEBUG);

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

    public Runnable compute(PopCount pop) {
        return () -> groupBuilder.group(pop);
    }

    public Stream<IndexGroup> groups() {
        return PopCount.TABLE.stream().map(groupBuilder::group);
    }

    @Test
    public void buildGroups() {


        timer("groups", () -> {

           // prefetch
            List<? extends ForkJoinTask<?>> tasks = PopCount.TABLE
                    .subList(50,100)
                    .transform(e -> ForkJoinTask.adapt(compute(e)))
                    .copyOf();

            ForkJoinTask.adapt(()->ForkJoinTask.invokeAll(tasks)).fork();

            long total = 0;

            for (IndexBuilder builder : groupBuilder.builders()) {
                boolean exists = builder.cached() != null;
                IndexGroup group = builder.get();

                total += group.range();

                System.out.format("%s %sgroups: %d\n",
                        group.pop(),
                        exists ? "ready " : "",
                        group.group().size());

                group.group().forEach((clop, c2t) -> {
                    System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
                });

                System.out.format("total: %d\n", total);
            }
            return null;
        });

        System.out.format("memory: %dMb\n", Runtime.getRuntime().totalMemory()/1024/1024);
        groupBuilder.builders().forEach(CachedBuilder::clear);
        Runtime.getRuntime().gc();
        System.out.format("memory: %dMb\n", Runtime.getRuntime().totalMemory()/1024/1024);

        //List<Integer> dummy = List.of(42,43, 44);
        //while(true) {
        //    List<Integer> tmp = new ArrayList<>(dummy);
        //    tmp.addAll(dummy);
        //    dummy=tmp;
        //    System.err.println("size: " + tmp.size());
        //}
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
