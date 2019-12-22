package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.10.19
 * Time: 15:57
 */
public class FileGroup extends LayerGroup<ScoreFile> {

    final IndexProvider indexes;

    final File dir;

    private FileGroup(IndexProvider indexes, File dir, Layer layer) {
        this(indexes, dir, layer.pop(), layer.player());
    }

    private FileGroup(IndexProvider indexes, File dir, PopCount pop, Player player) {
        super(pop, player, new HashMap<>());
        this.indexes = indexes;
        this.dir = dir;
    }

    void create() throws IOException {

        if (dir.exists()) {
            if (!dir.isDirectory())
                throw new FileAlreadyExistsException("file already exist: " + dir.toString());
        }

        dir.mkdir();

        if (!dir.isDirectory())
            throw new NotDirectoryException(dir.toString());
    }

    boolean exists() {
        return dir.isDirectory();
    }

    private ScoreFile file(PopCount clop) {

        return new ScoreFile() {

            @Override
            public PopCount pop() {
                return pop;
            }

            @Override
            public Player player() {
                return player;
            }

            @Override
            public PopCount clop() {
                return clop;
            }

            @Override
            public File file(String ext) {
                String name = name(clop, ext);
                return new File(dir, name);
            }

            @Override
            public PosIndex index() {
                return indexes.build(pop(), clop());
            }
        };
    }

    private String name(PopCount clop, String ext) {
        if(clop!=null)
            return String.format("p%d%d%c%d%d.%s",
                    pop.nb(), pop.nw(),
                    player.key(),
                    clop.nb(), clop.nw(),
                    ext);
        else
            return String.format("p%d%d%c.%s",
                pop.nb(), pop.nw(),
                player.key(), ext);
    }
}
