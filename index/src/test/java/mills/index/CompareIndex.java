package mills.index;

import mills.bits.PopCount;
import mills.index.builder.CachedBuilder;
import mills.index.builder2.GroupBuilder;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * version:     IndexCompare$
 * created by:  d.stueken
 * created on:  02.03.2021 19:53
 * modified by: $
 * modified on: $
 */
public class CompareIndex {

    final CachedBuilder builder1 = new CachedBuilder();

    final GroupBuilder builder2 = new GroupBuilder();

    @Test
    public void testGroups() {
        PopCount.TABLE.stream().map(PopCount.P99::sub).forEach(this::testGroups);
    }

    @Test
    public void testGroup() {
        PopCount pop = PopCount.of(8,8);
        var fg2 = builder2.futureGroup(pop);
        var group1 = builder1.buildGroup(pop);
        var group2 = fg2.join().group;

        compareIndex(group1.get(PopCount.EMPTY), group2.get(PopCount.EMPTY), 227824018);
    }

    void testGroups(PopCount pop) {
        System.out.format("testGroups %s\n", pop);

        var fg2 = builder2.futureGroup(pop);
        var group1 = builder1.buildGroup(pop);
        var group2 = fg2.join().group;

        assertEquals(group1.keySet(), group2.keySet());

        var fullTest = fg2.thenAcceptBothAsync(
                CompletableFuture.supplyAsync(() -> builder1.build(pop)),
                this::compareIndexes);

        group1.keySet().parallelStream().forEach(clop -> compareIndexes(group1.get(clop), group2.get(clop)));

        fullTest.join();
    }

    void compareIndexes(PosIndex ix1, PosIndex ix2) {
        System.out.format("testIndex  %s-%-4s %,13d\n", ix1.pop(), ix1.clop(), ix1.range());

        if(ix1.range()!=ix2.range()) {
            IntStream.range(0, ix1.range()).forEach(index->compareIndex(ix1, ix2, index));
            assertEquals(ix1.range(), ix2.range());
        }

        //assertEquals(ix1.range(), ix2.range());
        IntStream.range(0, ix1.range()).parallel().forEach(index->compareIndex(ix1, ix2, index));
    }

    void compareIndex(PosIndex ix1, PosIndex ix2, int pi0) {
        long i201 = ix1.i201(pi0);
        int pi2 = ix2.posIndex(i201);

        assertTrue(pi2>=0);

        i201 = ix2.i201(pi2);
        int pi1 = ix1.posIndex(i201);
        assertEquals(pi1, pi0);
    }
}
