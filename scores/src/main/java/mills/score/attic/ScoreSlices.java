package mills.score.attic;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.score.Score;
import mills.stones.Stones;

import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.05.13
 * Time: 17:04
 */
public class ScoreSlices {

    public final ScoreMap map;

    public final List<ScoreSlice> slices;

    ScoreSlices(ScoreMap map, List<ScoreSlice> slices) {
        this.map = map;
        this.slices = slices;
    }

    public String toString() {
        return String.format("ScoreSlices %s (%d)", map, max());
    }

    public static ScoreSlices of(final ScoreMap map) {
        return new ScoreSlices(map, ScoreSlice.slices(map));
    }

    /**
     * Lookup a score based on a i201 mask.
     *
     * @param i201 position to look up.
     * @return current score or count down
     */
    public int getScore201(long i201) {
        int index = map.posIndex(i201);
        final ScoreSlice slice = slices.get(index >>> 15);
        final short offset = (short) (index & Short.MAX_VALUE);
        return slice.getScore(offset);
    }

    public ScoreSlice getSlice(int posIndex) {
        return slices.get(posIndex / ScoreSlice.SIZE);
    }

    public IndexProcessor stuck() {
        // no stuck if jumping
        if (map.pop().max() < 4)
            return null;

        return new IndexProcessor() {

            final Player player = map.player();

            @Override
            public void process(int posIndex, long i201) {

                int self = Stones.stones(i201, player);
                int other = Stones.stones(i201, player.opponent());

                if (!anyMove(self, other)) {
                    ScoreSlice slice = slices.get(posIndex >>> 15);
                    final short offset = (short) (posIndex & Short.MAX_VALUE);

                    assert slice.getScore(offset) == 0 : "concurrent update of stuck position";

                    slice.setScore(offset, Score.LOST.value);
                }
            }

            // see if player may move any stone
            boolean anyMove(int self, int other) {
                return map.moves(player).any(other, self, self);
            }
        };
    }

    /**
     * Determine max value.
     * Has side effects:
     * Finish all pending actions.
     * Update map.max value.
     *
     * @return current max score of all slices.
     */
    public int max() {
        int max = 0;
        for (ScoreSlice slice : slices) {
            int m = slice.max();
            if(m>max)
                max = m;
        }

        return max;
    }

    RecursiveTask<ScoreStat> close() {
        return new RecursiveTask<>() {

            @Override
            protected ScoreStat compute() {

                int max = max();

                final ScoreStat stat = new ScoreStat(max + 1);

                List<RecursiveAction> tasks = slices.stream().map(stat::closer).collect(Collectors.toList());

                invokeAll(tasks);

                map.close();

                return stat;
            }
        };
    }
}
