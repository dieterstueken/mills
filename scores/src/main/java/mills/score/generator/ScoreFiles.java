package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:21
 */
public class ScoreFiles {

    final IndexProvider indexes;

    private final File root;

    public ScoreFiles(IndexProvider indexes, File root) {
        this.indexes = indexes;
        this.root = root;
    }

    public FileGroup group(PopCount pop, Player player) throws IOException {
        return FileGroup.of(indexes, root, pop, player);
    }
}
