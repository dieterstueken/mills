package mills.score.generator;

import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.util.AbstractRandomArray;
import mills.util.QueueActor;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User:
 * stueken
 * Date: 17.11.12
 * Time: 19:43
 */
public class MapSlice extends ScoreSlice {

    final ScoreMap scores;

    final QueueActor<MapSlice> work = new QueueActor<>(this);

    // max score occurred
    private int max = 0;

    // any positions set
    public final long[] any = new long[256];

    public ScoreMap scores() {
        return scores;
    }

    public int max() {
        return max;
    }

    public boolean any(final int score) {
        return any[score]!=0;
    }

    public void close() {
        super.close();
        work.finish();
    }

    protected MapSlice(ScoreMap scores, int index) {
        super(index);

        this.scores = scores;
    }

    public String toString() {
        return String.format("%s@%d (%d)", scores, sliceIndex(), max);
    }

    public int getScore(short offset) {
        int value = super.getScore(offset);

        if(value>max) // should not happen if map.max was updated properly
            value -= 256;

        return value;
    }

    public void submit(Consumer<MapSlice> action) {
        work.submit(action);
    }

    /**
     * Process any dirty blocks of score.
     * @param processor to process
     * @param score to analyze
     * @return previous dirty flags.
     */
    public long processAny(IndexProcessor processor, int score) {

        final long dirty = any[score];

        if(dirty==0)
            return 0;

        any[score] = 0;

        if(dirty==-1) {
            // process all
            processAll(processor);
            return dirty;
        }

        int start = base;
        final int next = base+size();
        long todo = dirty;

        while(todo!=0) {
            final int skip = Long.numberOfTrailingZeros(todo);

            if(skip>0) {
                start += skip*BLOCK;
                todo >>>= skip;
            }

            final int len = Long.numberOfTrailingZeros(~todo);
            assert len<64;
            todo >>>= len;

            final int end = Math.min(start + len*BLOCK, next);

            assert end>start : "empty range";

            scores.process(processor, start, end);
            start += len*BLOCK;
        }

        return dirty;
    }

    public void setScore(short offset, int score) {

        //if(score==1)
        //    System.currentTimeMillis();

        // scores < 0 just pass by
        if (score > max)
            max = score;

        if(score>0)
            any[score] |= mask(offset);
        else if(score<0)
            any[0] |= mask(offset);

        byte value = (byte) (score&0xff);

        if(value > max) {
            String error = String.format("%s %d=%d: %d exceeds max %d",
                    toString(), offset, posIndex(offset), value, max);

            throw new IllegalArgumentException(error);
        }

        int posIndex = posIndex(offset);
        scores.setScore(posIndex, score);
    }

    //////////////////////////////////////

    public static MapSlice newSlice(ScoreMap scores, int index) {
        return new MapSlice(scores, index);
    }

    public static int sliceCount(ScoreSet scores) {
        return (scores.size() + SIZE - 1) / SIZE;
    }

    public static int sliceCount(PosIndex index) {
        return (index.range() + SIZE - 1) / SIZE;
    }

    static List<MapSlice> slices(ScoreMap scores) {

        final int count = sliceCount(scores);

        return AbstractRandomArray.generate(count, index -> newSlice(scores, index));
    }

}
