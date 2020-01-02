package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.score.Score;
import org.junit.Test;

import java.nio.ByteBuffer;

public class MovedGroupTest {
    static final IndexProvider indexes = IndexProvider.load().lazy();

    PopCount p33 = PopCount.of(3,3);
    Player w = Player.White;

    @Test
    public void process() {
        MovingGroups target = MovingGroups.create(p33, w, this::moved, this::closed);

        System.out.format("%9s: %9d\n", target.moved, target.moved.range());

        Score score = Score.LOST;

        while(true) {
            int count = target.propagate(target, score);

            System.out.format("%9s: %9d\n", score, count);

            if(count==0)
                break;
            else
                score = score.next();
        }

        //MovedPosition pos = target.position(target, 562950021120016L);
        //pos.toString();
    }
    
    ScoreMap moved(PopCount clop) {
        PosIndex index = indexes.build(p33, clop);
        ByteBuffer buffer = ByteBuffer.allocateDirect(index.range());
        return new ScoreMap(index, Player.White, buffer);
    }

    LostSet closed(PopCount clop) {
        PosIndex index = indexes.build(p33, clop);
        return new LostSet(index, w);
    }
}