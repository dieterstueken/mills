package mills.scores.opening;

import mills.bits.Player;
import mills.position.Position;
import mills.position.Situation;
import mills.scores.ScoreMap;
import mills.scores.ScoreSlice;
import mills.util.AbstractRandomArray;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.10.13
 * Time: 11:58
 */
public class Slices extends AbstractRandomArray implements AutoCloseable {

    public final ScoreMap map;

    public final List<Slice> slices;

    public Slices(ScoreMap map) {
        super(ScoreSlice.sliceCount(map));
        this.map = map;
        this.slices = new ArrayList<>(size());
        for (int index = 0; index < size(); index++)
            slices.add(new Slice(map, index));
    }

    public Situation situation() {
        return map.situation();
    }

    public Player player() {
        return map.player();
    }

    public Position position(long i201) {
        return map.position(i201);
    }

    public int count() {
        int count = 0;

        for(Slice s:slices) {
            count += s.count;
            s.count = 0;
        }

        return count;
    }

    public void close() throws IOException {
        map.close();
    }

    @Override
    public Slice get(int index) {
        return slices.get(index);
    }

    public Slice getSlice(int posIndex) {
        return slices.get(posIndex / ScoreSlice.SIZE);
    }

    public int push(long i201) {
        int posIndex = map.posIndex(i201);
        int score = map.getScore(posIndex);

        if (score == 0)
            getSlice(posIndex).push(posIndex);

        return score;
    }

    public int pull(long i201) {
        int posIndex = map.posIndex(i201);
        return map.getScore(posIndex);
    }

    public void flush() {

        new Thread(String.format("flush %s", map.toString())) {
            public void run() {
                map.force();
            }
        }.start();

        Thread.yield();
    }

    RecursiveAction task(Function<Slice, Runnable> factory) {

        return new RecursiveAction() {

            final DateFormat df = new SimpleDateFormat("HH:mm:ss");
            public String now() {
                return df.format(new Date());
            }

            ForkJoinTask<?> task(Slice slice) {
                return new RecursiveAction() {

                    @Override
                    protected void compute() {
                        factory.apply(slice).run();
                    }
                };
            }

            @Override
            protected void compute() {
                System.out.format("%s %s start\n", now(), map.situation());

                List<ForkJoinTask<?>> tasks = slices.stream()
                        .map(this::task)
                        .collect(Collectors.toList());

                invokeAll(tasks);

                int size = map.size();
                int count = count();
                double db = count>0 ? 10*Math.log10((double)size/count) : 0.0;

                System.out.format("%s %s done%5.1f %,d/%,d\n", now(), map.situation(), db, count, size);

                flush();
            }
        };
    }
}
