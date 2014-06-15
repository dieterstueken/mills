package mills.scores.opening2;

import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Situation;
import mills.stones.MoveProcessor;
import mills.stones.Stones;

import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.01.14
 * Time: 20:16
 */
public class HitAnalyzer extends RecursiveTask<HitMap> {

    final Situation situation;

    final PosIndex index;

    final MoveProcessor puts;

    final MoveProcessor hits;

    private HitAnalyzer(Situation situation, PosIndex index, MoveProcessor puts, MoveProcessor hits) {
        this.situation = situation;
        this.index = index;
        this.puts = puts;
        this.hits = hits;
    }

    public static HitAnalyzer create(Situation situation, PosIndex index, MoveProcessor puts, MoveProcessor hits) {
        return new HitAnalyzer(situation, index, puts, hits);
    }

    public String toString() {
        return String.format("HitAnalyzer %s -%s", situation, situation.popTaken());
    }

    @Override
    protected HitMap compute() {

        Computer computer = new Computer();

        index.process(computer);

        return computer.result();
    }

    private class Computer implements IndexProcessor {

        final BitMap bits = new DirectBitMap(index.size());

        int count = 0;

        @Override
        public void process(int posIndex, long i201) {

            // backtrace: analyze move of other player

            int stay = Stones.stones(i201, situation.player);
            int move = Stones.stones(i201, situation.player.other());

            boolean hit = puts.process(stay, move);

            if(!hit)
                hit |= hits.process(stay, move);

            if(hit) {
                bits.set(posIndex);
                ++count;
            }
        }

        HitMap result() {
            if(count==0)
                return HitMap.empty(situation, index);

            if(count==index.size())
                return HitMap.full(situation, index);

            return new HitMap(situation, index, bits, count);
        }

        public String toString() {
            return String.format("HitComputer %s -%s %d", situation, situation.popTaken(), count);
        }
    }
}
