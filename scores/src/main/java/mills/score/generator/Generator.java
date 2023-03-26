package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.score.Score;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.10.19
 * Time: 17:31
 */
public class Generator {

    static final Logger LOGGER = Logger.getLogger(Generator.class.getName());

    final IndexProvider indexes;

    final ScoreFiles files;

    final Map<PopCount, GroupGenerator> generated = new ConcurrentHashMap<>();

    public Generator(IndexProvider indexes, File root) throws IOException {
        this.indexes = indexes;
        this.files = new ScoreFiles(root);
    }

    GroupGenerator generate(PopCount pop) {
        if (pop.min() < 3)
            throw new IllegalArgumentException();

        return generated.computeIfAbsent(pop, this::newGenerator).submit();
    }

    public void close() {
        generated.values().forEach(ForkJoinTask::join);
    }

    private GroupGenerator newGenerator(PopCount pop) {
        return new GroupGenerator(this, pop);
    }

    ScoreMap load(PosIndex index, Player player) {
        try {
            return files.map(index, player, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void save(ScoreMap scores) {

        try {
            int blocks =  files.save(scores);

            if(LOGGER.isLoggable(Level.INFO)) {
                int won = 0;
                int lost = 0;
                int dawn = 0;
                int max = 0;

                for(int posIndex=0; posIndex<scores.size(); ++posIndex) {
                    int score = scores.getScore(posIndex);
                    if(Score.isWon(score))
                        ++won;
                    else if(Score.isLost(score))
                        ++lost;
                    else
                        ++dawn;
                    if(score>max)
                        max = score;
                }

                LOGGER.log(Level.INFO, String.format("saved %s D:%,d W:%,d L:%,d M:%d %,d/%,d",
                        scores, dawn, won, lost, max, blocks, (scores.size()+4095)/4096));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void generateAll() {
        for(int nb=3; nb<10; ++nb) {
            for(int nw=3; nw<=nb; ++nw) {
                    PopCount pop = PopCount.get(nb, nw);
                GroupGenerator group = new GroupGenerator(this, pop);
                generated.put(pop, group);
                if(group.exists()) {
                    LOGGER.log(Level.INFO, ()->String.format("exists: %s", pop));
                } else {
                    group.submit().join();
                }

                generated.keySet().removeIf(cleanup(pop));
            }
        }
    }

    private Predicate<? super PopCount> cleanup(PopCount pop) {
        return key -> {
            PopCount diff = pop.sub(key);
            if(diff==null || diff.nb()<1 || diff.nw()<2)
                return false;

            LOGGER.log(Level.INFO, ()->String.format("drop: %s", key));

            return true;
        };
    }

    public static void main(String ... args) throws IOException {
        //int nb = Integer.parseInt(args[0]);
        //int nw = Integer.parseInt(args[1]);
        //PopCount pop = PopCount.get(nb, nw);

        File file = args.length<1 ? new File("build/scores") : new File(args[0]);
        IndexProvider indexes = IndexProvider.load();

        Generator generator = new Generator(indexes, file);

        generator.generateAll();
        
        //generator.close();
    }
}
