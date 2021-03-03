package mills.index;

import mills.bits.PopCount;
import mills.index.builder2.GroupBuilder;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * version:     IndexCompare$
 * created by:  d.stueken
 * created on:  02.03.2021 19:53
 * modified by: $
 * modified on: $
 */
public class IndexTests2 {

    final GroupBuilder builder = new GroupBuilder();

    @Test
    public void buildGroups() {

        try(GroupBuilder groupBuilder = timer("init", GroupBuilder::create)) {
            //timer("prepare", () -> {
            //    PopCount.TABLE.forEach(groupBuilder::futureGroup);
            //    return null;
            //});

            timer("groups", () -> {
                PopCount.TABLE.stream()
                        .map(PopCount.P99::sub)
                        .map(groupBuilder::futureGroup)
                        .map(CompletableFuture::join)
                        .forEach(group -> {
                            System.out.format("%s groups: %d\n", group.pop(), group.group.size());

                            group.group.forEach((clop, c2t) -> {
                                System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
                            });

                            System.out.println();
                        });
                return null;
            });
        }
    }

    void testGroups(PopCount pop) {
        System.out.format("testGroups %s\n", pop);

        var index = builder.build(pop);

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
