package mills.scores.opening;

import mills.position.Situation;
import mills.scores.ScoreFiles;
import mills.scores.ScoreMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.10.13
 * Time: 11:04
 */
public class Opening implements Runnable {

    public boolean exists(Situation situation) {
        return files.exists(situation);
    }

    Callable<Slices> openSlices(Situation situation) {
        ScoreMap map = open(situation);
        return () -> new Slices(map);
    }

    ScoreMap open(Situation situation) {
        try {
            if (situation.stock > 0)
                return files.createMMap(situation);

            return files.openMap(situation, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void run() {

        try (Level level = new Level(this, Level.MAX)) {
            level.slices(Situation.start()).push(0);
            level.invoke();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    final ScoreFiles files;

    Opening(String... args) {
        files = new ScoreFiles(args);
    }

    public static void main(String... args) {
        Opening main = new Opening(args);
        ForkJoinPool.commonPool().submit(main).join();
    }
}
