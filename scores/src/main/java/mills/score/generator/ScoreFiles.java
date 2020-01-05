package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;

import static java.nio.file.StandardOpenOption.*;

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

        root.mkdirs();

        if (!root.isDirectory())
            throw new NotDirectoryException(root.toString());
    }

    void save(ScoreMap scores) throws IOException {

        String name = String.format("%s%c%s.scores", scores.pop(), scores.player().key(), scores.clop());
        File file = new File(root, name);

        if(file.exists())
            throw new FileAlreadyExistsException("file already exist: " + file.toString());

        try(FileChannel fc = FileChannel.open(file.toPath(), CREATE, WRITE)) {
            fc.write(scores.scores);
        }
    }

    public ScoreMap load(PosIndex index, Player player) throws IOException {

        String name = String.format("%s%c%s.scores", index.pop(), player.key(), index.clop());
        File file = new File(root, name);

        if(file.length() != index.range())
            throw new IOException("unexpected file size: " + file.toString());

        try(FileChannel fc = FileChannel.open(file.toPath(), READ)) {
            int size = index.range();
            ByteBuffer scores = ByteBuffer.allocateDirect(size);
            size -= fc.read(scores);

            if(size!=0)
                throw new IOException("incomplete read: " + file.toString());

            return new ScoreMap(index, player, scores);
        }
    }

    public ScoreMap map(PosIndex index, Player player, boolean readonly) throws IOException {

        String name = String.format("%s%c%s.scores", index.pop(), player.key(), index.clop());
        File file = new File(root, name);
        int size = index.range();

        if (file.length() != size)
            throw new IOException("unexpected file size: " + file.toString());

        FileChannel fc = FileChannel.open(file.toPath(), readonly ? READ : WRITE);
        MappedByteBuffer scores = fc.map(readonly ? FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE, 0, size);

        return new ScoreMap(index, player, scores);
    }
}
