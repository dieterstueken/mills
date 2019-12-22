package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:21
 */
public class ScoreFiles {

    private final File root;

    public ScoreFiles(File root) throws IOException {
        this.root = root;

        if (root.exists()) {
            if (!root.isDirectory())
                throw new FileAlreadyExistsException("file already exist: " + root.toString());
        }

        root.mkdir();

        if (!root.isDirectory())
            throw new NotDirectoryException(root.toString());
    }
}
