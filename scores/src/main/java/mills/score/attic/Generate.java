package mills.score.attic;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.position.Situation;
import mills.score.Pair;
import mills.score.Score;
import mills.score.ScoreMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.10.13
 * Time: 12:49
 */
public class Generate implements Runnable {

    final ScoreFiles files;

    public Generate(String ... args) {
        files = new ScoreFiles(args);
    }

    public ScoreMap descend(Situation situation) throws IOException {

        Situation descend = situation.hit(situation.player);
        if (descend == null || !descend.popStock().valid())
            return null;

        ScoreMap downMap = files.loadMap(descend);
        if (downMap != null)
            return downMap;

        downMap = files.loadMap(descend.swap());

        if (downMap != null)
            return downMap;

        throw new FileNotFoundException(descend.toString());
    }

    public static Pair<Situation> situations(final PopCount pop) {
        Situation white = Situation.of(pop, Player.White);
        Situation black = pop.equals() ? white : Situation.of(pop, Player.Black);
        return Pair.of(white, black);
    }

    // create already forked tasks
    final Function<Situation, ForkJoinTask<ScoreSlices>> slices = situation -> new RecursiveTask<ScoreSlices>() {
        @Override
        public ScoreSlices compute() {
            ScoreMap map = files.createMap(situation);
            return ScoreSlices.of(map);
        }
    }.fork();

    final Function<Situation, ForkJoinTask<ScoreMap>> downMap = input -> new RecursiveTask<ScoreMap>() {
        @Override
        protected ScoreMap compute() {
            try {
                return descend(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }.fork();

    final Pair<ScoreSlices> elevate(final Pair<Situation> situations) {

        final Pair<ForkJoinTask<ScoreSlices>> slices = situations.map(this.slices);

        return slices.parallel2(
                new Function<Pair<ForkJoinTask<ScoreSlices>>, ForkJoinTask<ScoreSlices>>() {
                    @Override
                    public ForkJoinTask<ScoreSlices> apply(final Pair<ForkJoinTask<ScoreSlices>> slices) {
                        try {
                            ScoreSlices self = slices.self.join();
                            ScoreMap downMap = descend(self.map.situation());
                            ScoreSlices other = slices.other.join();
                            return ScoreElevator.of(self, other, downMap);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    int max(Pair<ScoreSlices> slices) {
        int self = slices.self.max();
        int other = slices.equal() ? self : slices.other.max();

        return Math.max(self, other);
    }

    static Function<Pair<ScoreSlices>, ForkJoinTask<ScoreSlices>> moveScores(final int score) {
        return input -> ScoreMover.of(input, score);
    }

    static Function<Pair<ScoreSlices>, ForkJoinTask<ScoreSlices>> closeScores(final int score) {
        return input -> ScoreCloser.of(input, score);
    }

    Pair<ScoreStat> generate(final Pair<Situation> situations) {

        Pair<ScoreSlices> slices = elevate(situations);
        System.out.format("%s %s elevated %d:%d\n", now(), situations, slices.self.max(), slices.other.max());

        for (int score = 1; max(slices) >= score; ++score) {

            System.out.format("%s %s %d\n", now(), situations, score);

            if (Score.isLost(score+1))
                slices = slices.parallel2(closeScores(score+1));

            slices = slices.parallel2(moveScores(score));
        }

        return slices.parallel1(ScoreSlices::close);
    }

    private boolean exists(Pair<Situation> situations) {
        return files.exists(situations.self) && (situations.equal() || files.exists(situations.other));
    }

    static final Predicate<PopCount> FILTER = pop -> pop != null && pop.valid() && pop.nb <= pop.nw;

    final DateFormat df = new SimpleDateFormat("HH:mm:ss");
    public String now() {
        return df.format(new Date());
    }

    public void run() {

        PopCount.TABLE.stream().filter(FILTER).sorted(Comparator.comparing(PopCount::max)).forEach(pop -> {

            final Pair<Situation> situations = situations(pop);

            if (exists(situations)) {
                System.out.format("%s %s exists\n", now(), situations);
            } else {

                System.out.format("%s %s start\n", now(), situations);
                long millis = System.currentTimeMillis();

                Pair<ScoreStat> stats = generate(situations);

                System.out.format("%s %s stat\n", now(), situations.self.toString());
                stats.self.print();
                if(!stats.equal()) {
                    System.out.format("%s %s stat\n", now(), situations.other.toString());
                    stats.other.print();
                }

                millis = System.currentTimeMillis() - millis;

                System.out.format("%s %s done %ds\n\n", now(), situations, millis/1000);
            }
        });
    }

    public static void main(String... args) {
        Generate main = new Generate(args);
        ForkJoinPool.commonPool().submit(main).join();
    }
}
