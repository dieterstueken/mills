package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.score.Score;
import org.junit.Test;

import java.nio.ByteBuffer;

public class MovedGroupTest {
    static final IndexProvider indexes = IndexProvider.load();

    PopCount p33 = PopCount.of(3,3);
    Player w = Player.White;

    @Test
    public void process() {
        MovedGroup target = moved();

        System.out.format("%9s: %9d\n", target, target.range());

        Score score = Score.LOST;

        while(true) {
            int count = target.propagate(target, score);

            System.out.format("%9s: %9d\n", score, count);

            if(count==0)
                break;
            else
                score = score.next();
        }

        MovedPosition pos = target.position(target, 562950021120016L);
        pos.toString();
    }

    MovedGroup moved() {
        return MovedGroup.moved(p33, w, closed(), this::moved);
    }

    ScoreMap moved(Clops clops) {
        PosIndex index = indexes.build(clops);
        ByteBuffer buffer = ByteBuffer.allocateDirect(index.range());
        return new ScoreMap(index, Player.White, buffer);
    }

    ClosedGroup closed() {
        return ClosedGroup.closed(p33, w, this::closed);
    }

    LostSet closed(Clops clops) {
        PosIndex index = indexes.build(clops);
        return new LostSet(index, w);
    }
}