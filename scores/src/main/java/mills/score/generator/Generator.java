package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
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

    final Map<PopCount, GroupsGenerator> layers = new ConcurrentHashMap<>();

    public Generator(IndexProvider indexes, File root) throws IOException {
        this.indexes = indexes;
        this.files = new ScoreFiles(root);
    }

    GroupsGenerator generate(PopCount pop) {
        if (pop.min() < 3)
            throw new IllegalArgumentException();

        return layers.computeIfAbsent(pop, this::newGenerator).submit();
    }

    public void close() {
        layers.values().forEach(ForkJoinTask::join);
    }

    private GroupsGenerator newGenerator(PopCount pop) {
        return new GroupsGenerator(this, pop);
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
            int blocks = files.save(scores);
            LOGGER.log(Level.FINER, String.format("saved %s %,d/%,d", scores, blocks, (scores.size()+4095)/4096));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void generateAll() {
        for(int nb=3; nb<10; ++nb) {
            for(int nw=3; nw<=nb; ++nw) {
                    PopCount pop = PopCount.get(nb, nw);
                GroupsGenerator group = generate(pop);
                // generate synchronously.
                group.join();

                layers.values().forEach(g -> cleanup(pop, g));
            }
        }
    }

    private void cleanup(PopCount pop, GroupsGenerator group) {
        PopCount diff = pop.sub(group.pop);
        if(diff!=null && diff.nb()>=1 && diff.nw()>=2) {
            group.clear();
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
