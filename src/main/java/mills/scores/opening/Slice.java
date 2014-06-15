package mills.scores.opening;

import mills.index.IndexProcessor;
import mills.scores.MapSlice;
import mills.scores.ScoreMap;
import mills.util.QueueActor;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.13
 * Time: 18:25
 */
public class Slice extends MapSlice {

    QueueActor<Slice> work = new QueueActor<>(this);

    long dirty = 0;

    int count = 0;

    protected Slice(ScoreMap map, int index) {
        super(map, index);
    }

    void push(int posIndex) {
        work.submit(slice -> slice.mark(posIndex));
    }

    void finish() {
        QueueActor<Slice> queue = work;
        if(queue!=null) {
            // reset: no work accepted any more
            work = null;
            assert queue.idle() : "still working";
        }
    }

    void mark(int posIndex) {

        int score = map.getScore(posIndex);

        if(score==0) {
            map.setScore(posIndex, -1);
            short offset = offset(posIndex);
            long mask = mask(offset);
            dirty |= mask;
            ++count;
        }
    }

    /**
     * Process any dirty blocks of score.
     *
     * @param processor to process
     * @return previous dirty flags.
     */
    public void processAny(IndexProcessor processor) {

        if (dirty == 0)
            return;

        int start = base;
        final int next = base + size();
        long todo = dirty;
        dirty = 0;

        if(todo==-1)
            processAll(processor);
        else
        while (todo != 0) {
            final int skip = Long.numberOfTrailingZeros(todo);

            if (skip > 0) {
                start += skip * BLOCK;
                todo >>>= skip;
            }

            final int len = Long.numberOfTrailingZeros(~todo);
            assert len < 64;
            todo >>>= len;

            final int end = Math.min(start + len * BLOCK, next);

            assert end > start : "empty range";

            map.process(processor, start, end);
            start += len * BLOCK;
        }
    }
}
