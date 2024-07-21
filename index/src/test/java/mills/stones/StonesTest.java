package mills.stones;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.index.builder.IndexGroups;
import mills.position.Positions;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.09.23
 * Time: 16:35
 */
class StonesTest {

    IndexProvider provider = new IndexGroups();
    
    @Test
    void closed() {

        BitSet total = new BitSet();

        for(int i=0; i<10; ++i) {
            BitSet mask = closed(i);
            System.out.format("%d: %d\n", i, mask.cardinality());
            total.or(mask);
        }

        System.out.format("total: %d\n", total.cardinality());
    }

    BitSet closed(int i) {
        PosIndex posIndex = provider.build(PopCount.get(0, i));

        BitSet mask = new BitSet();

        posIndex.process((index, i201) -> {
            int stones = Stones.stones(i201, Player.White);
            int closed = Stones.closed(stones);
            int clop = Positions.clop(i201).nw;
            int m = 10*clop + Integer.bitCount(closed);
            m += 100 * Integer.bitCount(Stones.closes(stones));

            if(!mask.get(m))
                mask.set(m);
        });

        return mask;
    }
}