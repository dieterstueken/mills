package mills.scores.opening;

import mills.position.Situation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.10.13
 * Time: 11:58
 */
public class Level extends RecursiveAction implements AutoCloseable {

    public static final int MAX = 18;

    final Opening files;

    final int stock;

    final ConcurrentHashMap<Situation, Supplier<Slices>> slices = new ConcurrentHashMap<>();

    final DateFormat df = new SimpleDateFormat("HH:mm:ss");
    public String now() {
        return df.format(new Date());
    }

    public String toString() {
        return String.format("Level %d", stock);
    }

    public Level(Opening files, int stock) {
        this.stock = stock;
        this.files = files;
    }

    public Slices slices(Situation situation) {

        if(!situation.popStock().valid())
            return null;

        // find either
        if (stock == 0 && !files.exists(situation)) {
            situation = situation.swap();
            if(!files.exists(situation))
                    return null;
        }

        // create new one
        return slices.computeIfAbsent(situation, this::newSlices).get();
    }

    private Supplier<Slices> newSlices(Situation situation) {

        ForkJoinTask<Slices> task = ForkJoinTask.adapt(files.openSlices(situation)).fork();

        return task::join;
    }

    public void close() throws IOException {

        for (Supplier<Slices> t : slices.values())
            t.get().close();
    }

    Function<Slice, Runnable> accept(BiFunction<Slice, Level, Runnable> worker) {
        return slice -> () -> worker.apply(slice, this).run();
    }

    void compute(Function<Slice, Runnable> runners) {

        List<ForkJoinTask<?>> tasks = slices.values().stream()
                .map(sp -> sp.get())
                .map(s -> s.task(runners))
                .collect(Collectors.toList());

        invokeAll(tasks);
    }

    public void compute() {

        System.out.format("%s push %d with %d\n", now(), stock, slices.size());

        assert stock > 0 : "empty stock";

        try (Level next = new Level(files, stock - 1)) {

            if(next.stock>0) {
                compute(next.accept(SliceWorker::push));
                next.flush();
                next.invoke();
            }

            System.out.format("%s pull %d with %d\n", now(), stock, slices.size());

            compute(next.accept(SliceWorker::pull));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Start a background thread to flush all data to disk.
     */
    private void flush() {

        new Thread(String.format("flush level %d", stock)) {
            public void run() {
                slices.values().stream().forEach(s->s.get().map.force());
            }
        }.start();

        Thread.yield();
    }
}
