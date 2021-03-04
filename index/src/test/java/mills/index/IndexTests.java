package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.builder.GroupBuilder;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
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

    final GroupBuilder groupBuilder = new GroupBuilder();

    public Stream<GroupBuilder.Group> groups() {
        return PopCount.TABLE.stream()
                .map(PopCount.P99::sub)
                .map(groupBuilder::futureGroup)
                .map(CompletableFuture::join);
    }

    @Test
    public void buildGroups() {

        timer("groups", () -> {
            groups().forEach(group -> {
                System.out.format("%s groups: %d\n", group.pop(), group.group.size());

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
