package mills.scores.opening2;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.stones.MoveProcessor;
import mills.stones.MoveTable;
import mills.stones.Stones;
import mills.util.AbstractRandomArray;

import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.01.14
 * Time: 17:54
 */
public class HitProcessor extends RecursiveTask<Integer> {

    public static final int SIZE = Short.MAX_VALUE + 1;

    final MoveProcessor analyzer;

    final PosIndex index;

    // next to move in forward direction
    final Player player;

    // result bit map after analysis
    final DirectBitMap hits;

    public HitProcessor(PosIndex index, Player player, MoveProcessor analyzer) {
        this.analyzer = analyzer;
        this.index = index;
        this.player = player;
        this.hits = new DirectBitMap(index.size());
    }

    @Override
    protected Integer compute() {

        List<SliceProcessor> processors = processors();

        invokeAll(processors);

        int count = 0;

        for(SliceProcessor p:processors)
            count += p.count;

        return count;
    }

    private List<SliceProcessor> processors() {
        int count = (index.size() + SIZE - 1) / SIZE;

        return new AbstractRandomArray<SliceProcessor>(count) {

            @Override
            public SliceProcessor get(int index) {
                return new SliceProcessor(index);
            }
        }.immutableCopy();
    }

    public class SliceProcessor extends RecursiveAction implements IndexProcessor {

        final int base;

        int count = 0;

        public SliceProcessor(int index) {
            this.base = index * SIZE;
        }

        @Override
        public void process(int posIndex, long i201) {

            // backtrace analysis after black has moved

            int black = Stones.stones(i201, player.other());
            int white = Stones.stones(i201, player);

            int closed = Stones.closed(black);

            if(closed==0)
                return;

            int count = MoveTable.TAKE.move(white, black, closed, analyzer);

            if(count!=0) {
                hits.set(posIndex);
                ++count;
            }
        }

        @Override
        protected void compute() {
            index.process(this, base, base+SIZE);
        }
    }
}
