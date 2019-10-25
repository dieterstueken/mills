package mills.scores.opening;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.position.Situation;
import mills.scores.Score;
import mills.stones.Stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.13
 * Time: 17:55
 */

/**
 * Class SliceWorker performs some Action&lt;Action&gt;
 * on a target level for each relevant position on MapSlice.
 */
public abstract class SliceWorker implements IndexProcessor, Runnable {

    final Slice slice;
    final Level next;

    final PutStone<SliceTarget> put;
    PutStone<SliceTarget> hit = null;

    public SliceWorker(Slice slice, Level next) {
        this.slice = slice;
        this.next = next;
        this.put = newPut();
    }

    int moved() {
        int moved = put.size();
        if (hit != null)
            moved += hit.size();

        return moved;
    }

    PutStone<SliceTarget> newPut() {
        Situation put = slice.map.situation().put(false);
        SliceTarget target = newTarget(put);
        boolean swap = target.situation().player().equals(Player.White);
        return PutStone.put(target, swap);
    }

    PutStone<SliceTarget> newHit() {
        Situation hit = slice.map.situation().put(true);
        SliceTarget target = newTarget(hit);
        boolean swap = target.situation().player().equals(Player.White);
        return PutStone.hit(target, swap);
    }

    abstract SliceTarget newTarget(Situation situation);

    public SliceTarget lost(Situation situation) {
        return new SliceTarget(situation) {
            @Override
            public long normalize(long i201) {
                return slice.map.normalize(i201);
            }

            @Override
            public int apply(long i201) {
                return Score.LOST;
            }
        };
    }

    @Override
    public void process(int posIndex, long i201) {

        final Player player = slice.player();
        final int black = Stones.stones(i201, player.other());
        final int white = Stones.stones(i201, player);
        final int free = Stones.free(black | white);
        final int closes = free & Stones.closes(white);

        put.move(black, white, free ^ closes);

        if (closes != 0) {
            if (hit == null)
                hit = newHit();

            hit.move(black, white, closes);
        } else if (hit != null)
            hit.clear();

        assert moved() != 0 : "no stones put";

        propagate(posIndex, i201, put);
        propagate(posIndex, i201, hit);
    }

    public void propagate(int posIndex, long i201, PutStone<? extends SliceTarget> moved) {

        if (moved != null)
            for (int i = 0; i < moved.size(); ++i) {
                long m201 = moved.get201(i);
                propagate(posIndex, i201, m201, moved.target);
            }
    }

    abstract void propagate(int posIndex, long i201, long m201, SliceTarget target);



    static SliceWorker push(Slice slice, Level next) {

        slice.finish();

        return new SliceWorker(slice, next) {

            SliceTarget newTarget(Situation situation) {

                if(situation.stock==0)
                    throw new IllegalStateException("push into finished level");

                Slices slices = next.slices(situation);
                return slices==null ? lost(situation) : new SliceTarget(situation) {
                    @Override
                    public long normalize(long i201) {
                        return slice.map.normalize(i201);
                    }

                    @Override
                    public int apply(long i201) {
                        return slices.push(i201);
                    }
                };
            }

            public String toString() {
                return String.format("push %s", slice.map.situation());
            }

            @Override
            public void run() {
                slice.processAll(this);
            }

            @Override
            void propagate(int posIndex, long i201, long m201, SliceTarget target) {
                target.apply(m201);
            }
        };
    }

    static SliceWorker pull(Slice slice, Level next) {

        return new SliceWorker(slice, next) {

            SliceTarget newTarget(Situation situation) {
                Slices slices = next.slices(situation);
                return slices==null ? lost(situation) : new SliceTarget(situation) {
                    @Override
                    public long normalize(long i201) {
                        return slice.map.normalize(i201);
                    }

                    @Override
                    public int apply(long i201) {
                        return slices.pull(i201);
                    }
                };
            }

            public String toString() {
                return String.format("pull %s", slice.map.situation());
            }

            int result = Score.LOST;

            @Override
            void propagate(int posIndex, long i201, long m201, SliceTarget target) {

                int score = target.apply(m201);
                if(score!=0)
                    ++score;

                if (Score.betterThan(score, result))
                    result = score;
            }

            @Override
            public void run() {
                slice.processAny(this);
            }

            @Override
            public void process(int posIndex, long i201) {

                // nothing to do
                if (slice.map.getScore(posIndex) == 0)
                    return;

                // reset score accumulation
                result = Score.LOST;

                // generate moves
                super.process(posIndex, i201);

                assert result!=Score.LOST : "nothing moved";

                slice.map.setScore(posIndex, result);

                if (result != 0)
                    ++slice.count;
            }
        };
    }
}
