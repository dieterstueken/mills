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

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.10.19
 * Time: 17:31
 */
public class Generator {

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
            files.save(scores);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String ... args) throws IOException {
        int nb = Integer.parseInt(args[0]);
        int nw = Integer.parseInt(args[1]);
        PopCount pop = PopCount.get(nb, nw);

        File file = args.length<3 ? new File("build/scores") : new File(args[2]);
        IndexProvider indexes = IndexProvider.load();

        Generator generator = new Generator(indexes, file);

        generator.generate(pop).join();
        
        generator.close();
    }
}
