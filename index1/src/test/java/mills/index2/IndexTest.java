package mills.index2;

import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.index1.IndexList;
import mills.position.Position;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 20.09.19
 * Time: 14:42
 */
public class IndexTest {

    @Test
    public void testPos02() {
        IndexBuilder builder = IndexBuilder.create();
        IndexList refBuilder = IndexList.create();

        PopCount pop = PopCount.get(3,0);

        PosIndex pi = builder.build(pop);
        PosIndex ri = refBuilder.get(pop);

        System.out.format("l%d%d%10d, %4d : %10d,%4d\n\n",
                pop.nb, pop.nw,
                pi.range(), pi.n20(),
                ri.range(), ri.n20());

        Map<Integer, Position> missing = new TreeMap<>();
        Map<Integer, Position> duplicates = new TreeMap<>();

        ri.process((index, i201) -> missing.put(index, Position.of(i201)));

        pi.process((index, p201)-> {
            Position pos = Position.of(p201);
            int refIndex = ri.posIndex(p201);
            System.out.format("%s: %d %d\n", pos, index, refIndex);

            if(missing.remove(refIndex)==null)
                duplicates.put(refIndex, pos);
        });

        System.out.format("missing: %d\n", missing.size());

        missing.entrySet().forEach(e->System.out.format("%s: %d\n", e.getValue(), e.getKey()));

        System.out.format("duplicates: %d\n", duplicates.size());

        duplicates.entrySet().forEach(e->System.out.format("%s: %d\n", e.getValue(), e.getKey()));

        System.out.println();
    }
}
