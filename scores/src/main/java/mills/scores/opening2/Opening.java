package mills.scores.opening2;

import mills.position.Situation;
import mills.scores.ScoreFiles;
import mills.scores.ScoreMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.10.13
 * Time: 11:04
 */
public class Opening extends ScoreFiles implements Runnable {

    ScoreMap openOrCreate(Situation situation) {
        try {
            if (situation.stock > 0)
                return createMMap(situation);

            return openMap(situation, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void run() {

        Level level = Level.start(this);
        level.invoke();

        while(level!=null) {
            level.stat(System.out);
            level = level.join();
        }
    }

    Opening(String... args) {
        super(args);
    }

    public static void main(String... args) {
        Opening main = new Opening(args);
        ForkJoinPool.commonPool().submit(main).join();
    }
}
