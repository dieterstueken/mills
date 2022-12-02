package mills.score.attic;

import mills.index.IndexProcessor;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.06.13
 * Time: 20:25
 */

/**
 * Process any dirty or all positions of source.
 * Modify source (direct) or target (queued/synchronized).
 * Return target.
 */
public abstract class ScoreWorker extends RecursiveTask<ScoreSlices> {

    final protected ScoreSlices source;

    final protected ScoreSlices target;

    public ScoreWorker(ScoreSlices source, ScoreSlices target) {
        this.source = source;
        this.target = target;
    }

    public String name() {
        return getClass().getSimpleName();
    }

    public String toString() {
        return String.format("%s %s -> %s", name(), source, target);
    }

    public int size() {
        return source.slices.size();
    }

    @Override
    protected ScoreSlices compute() {

        worker(new AtomicInteger()).invoke();

        return target;
    }

    private volatile int task=0;

    private RecursiveAction worker(final AtomicInteger counter) {

        return new RecursiveAction() {

            final int id = ++task;
            //int index = -1;

            private ForkJoinTask<?> parallel() {
                int left = size() - counter.get();
                return left<2 ? null : worker(counter).fork();
            }

            int next() {
                return counter.getAndIncrement();
            }

            @Override
            protected void compute() {

                int index = next();
                if(index>=size()) {
                    return;
                }

                final ForkJoinTask<?> parallel = parallel();

                final IndexProcessor processor = processor();

                do {
                    processSlice(processor, index);
                } while((index=next())<size());

                if(parallel!=null && !parallel.tryUnfork())
                    parallel.join();
            }
        };
    }

    abstract protected IndexProcessor processor();

    protected void processSlice(IndexProcessor processor, int index) {
        ScoreSlice slice = source.slices.get(index);
        slice.processAll(processor);
    }

    abstract protected void processSlice(IndexProcessor processor, ScoreSlice slice);

    abstract protected class Processor implements IndexProcessor {

        protected int lastIndex = -1;
        protected int submitted = 0;

        public String name() {
            return getClass().getSimpleName();
        }

        public String toString() {
            int index = lastIndex;

            if (index >= 0) {
                ScoreSlice slice = source.getSlice(lastIndex);
                short offset = slice.offset(lastIndex);

                return String.format("%s[%d] %d %+d",
                        name(), slice.sliceIndex(), offset, submitted);
            } else
                return String.format("%s[]", name());
        }

        public int execute(final ScoreSlice slice, int score) {
            if(score>0)
                slice.processAny(this, score);
            else
                slice.processAll(this);

            return submitted;
        }

        @Override
        public void process(int posIndex, long i201) {
            lastIndex = posIndex;

            ScoreSlice slice = source.getSlice(posIndex);
            short offset = slice.offset(posIndex);

            if(analyze(slice, offset, i201))
                ++submitted;
        }

        abstract boolean analyze(ScoreSlice slice, short offset, long i201);
    }
}
