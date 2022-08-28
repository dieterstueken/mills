package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.builder.GroupBuilder;
import org.junit.Test;

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

    static final GroupBuilder.Debug DEBUG = new GroupBuilder.Debug() {
        @Override
        public void start(final PopCount pop) {
            System.out.format("%s start\n", pop);
        }

        @Override
        public void done(final GroupBuilder.Group result) {
            System.out.format("%s done\n", result.pop());
        }
    };

    final GroupBuilder groupBuilder;

    {
        groupBuilder = new GroupBuilder(DEBUG);

        // prefetch
        List<? extends ForkJoinTask<?>> tasks = PopCount.TABLE
                .subList(50,100)
                .transform(this::compute)
                .transform(ForkJoinTask::adapt)
                .copyOf();

        ForkJoinTask.adapt(()->ForkJoinTask.invokeAll(tasks)).fork();
    }

    public Runnable compute(PopCount pop) {
        return () -> groupBuilder.group(pop);
    }

    public Stream<GroupBuilder.Group> groups() {
        return PopCount.TABLE.stream().map(groupBuilder::group);
    }

    @Test
    public void buildGroups() {

        timer("groups", () -> {
            groupBuilder.entries().forEach(entry -> {

                boolean exists = entry.cached()!=null;
                GroupBuilder.Group group = entry.get();

                System.out.format("%s %sgroups: %d\n",
                        group.pop(),
                        exists ? "ready " :"",
                        group.group.size());

                group.group.forEach((clop, c2t) -> {
                    System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
                });

                System.out.println();
            });
            return null;
        });
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
