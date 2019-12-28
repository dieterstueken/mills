package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.score.Score;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 23.12.19
 * Time: 12:29
 */
public class GroupGeneratorTest {

    static final IndexProvider indexes = IndexProvider.load();

    PopCount p33 = PopCount.of(3,3);

    PopCount c00 = PopCount.of(0,0);
    PopCount c10 = PopCount.of(1,0);
    PopCount c01 = PopCount.of(0,1);
    PopCount c11 = PopCount.of(1,1);

    PosIndex i33c00 = indexes.build(p33, c00);
    PosIndex i33c01 = indexes.build(p33, c01);
    PosIndex i33c10 = indexes.build(p33, c10);
    PosIndex i33c11 = indexes.build(p33, c11);

    Player b = Player.Black;
    Player w = Player.White;

    @Test
    public void process() {

        SlicesGroup<ScoreSlice> closed = closed();
        SlicesGroup<MapSlice> target = target();

        GroupGenerator generator = new GroupGenerator(target, closed);
        GroupGenerators generators = new GroupGenerators(generator);

        Score score = Score.LOST;

        while(true) {
            int n = generators.process(score);
            if(n==0)
                break;
            else
                score = score.next();
        }

        System.out.println(score);
    }

    SlicesGroup<MapSlice> target() {
        SlicesGroup<MapSlice> group = new SlicesGroup<>(p33, w, new HashMap<>());
        group.add(target(i33c00, w));
        group.add(target(i33c01, w));
        group.add(target(i33c10, w));
        group.add(target(i33c11, w));
        return group;
    }

    SlicesGroup<ScoreSlice> closed() {

        SlicesGroup<ScoreSlice> group = new SlicesGroup<>(p33, w, new HashMap<>());
        group.add(closed(i33c10, w));
        group.add(closed(i33c11, w));
        return group;
    }

    Slices<ScoreSlice> closed(PosIndex index, Player player) {

        ScoreSet scores = new ScoreSet(index, player) {

            @Override
            public String toString() {
                return String.format("lost(%s%c%s)", pop(), player.key(), clop());
            }

            @Override
            public int getScore(int index) {
                return Score.LOST.value;
            }

            @Override
            ScoreSlice openSlice(int index) {
                ScoreSlice slice = super.openSlice(index);
                slice.dirty[Score.LOST.value] = -1;
                slice.max=Score.LOST.value;
                return slice;
            }
        };

        return Slices.generate(scores, scores::openSlice);
    }

    Slices<MapSlice> target(PosIndex index, Player player) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(index.range());
        ScoreMap scores = new ScoreMap(index, player, buffer);
            //{
            //@Override
            //MapSlice openSlice(int index) {
            //    MapSlice slice = super.openSlice(index);
            //    slice.process((posIndex, i201) -> {
            //        int n = slice.unresolved(i201);
            //        if(n==0)
            //            slice.setScore(slice.offset(posIndex), Score.LOST.value);
            //    });
            //    return slice;
            //};
        return Slices.generate(scores, scores::openSlice);
    }
}