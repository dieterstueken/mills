package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:21
 */
public class ScoreFiles {

    private final File root;

    public ScoreFiles(File root, IndexProvider indexes) {
        this.root = root;
    }

    public FileGroup group(String prefix, PopCount pop, Player player) {
        return FileGroup.create(root, prefix, pop, player);
    }
}
