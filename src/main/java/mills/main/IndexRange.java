package mills.main;

import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.position.Positions;

import java.util.AbstractList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.RecursiveAction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 16.11.12
 * Time: 23:36
 */
public class IndexRange extends RecursiveAction {

    protected final Random random = new Random(123456789);
    public final IndexProvider indexes = IndexProvider.load();

    public void compute() {

        for (PopCount pop : PopCount.TABLE) {
            PosIndex pi = indexes.get(pop);
            verify(pi);
        }
    }

    private void verify(final PosIndex pi) {
        final int size = pi.range();

        System.out.format("verify %s\n", pi.pop());

        List<Runnable> tasks = new AbstractList<Runnable>() {
            public int size() {
                return 100;
            }

            public Runnable get(int i) {
                int i1 = random.nextInt(size);
                int i2 = random.nextInt(size);

                final int start = Math.min(i1, i2);
                final int end = Math.max(i1, i2);

                final IndexProcessor processor = new IndexProcessor() {
                    @Override
                    public void process(int posIndex, long i201) {

                        if (posIndex < start || posIndex >= end)
                            throw new IndexOutOfBoundsException();

                        long k201 = pi.i201(posIndex);
                        if ((k201 & Positions.M201) != (i201 & Positions.M201))
                            throw new RuntimeException();
                    }
                };

                return new Runnable() {

                    @Override
                    public void run() {
                        pi.process(processor, start, end);
                    }
                };
            }
        };

        for (Runnable r : tasks) {
            r.run();
        }
    }

    public static void main(String... args) {
        new IndexRange().invoke();
    }
}
