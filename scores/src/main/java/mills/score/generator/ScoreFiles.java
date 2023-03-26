package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.util.Set;

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
                throw new FileAlreadyExistsException("file already exist: " + root);
        }

        root.mkdirs();

        if (!root.isDirectory())
            throw new NotDirectoryException(root.toString());
    }

    public File file(Clops clops, Player player) {
        return file(clops.pop(), clops.clop(), player);
    }

    public File file(PopCount pop, PopCount clop, Player player) {
        String name = String.format("m%d%d%c%d%d.scores", pop.nb(), pop.nw(), player.key(), clop.nb(), clop.nw());
        return new File(root, name);
    }

    static final Set<OpenOption> LOAD = Set.of(READ);
    static final Set<OpenOption> SAVE = Set.of(CREATE, WRITE);

    static final int BLOCK = 1<<12;

    int save(ScoreMap scores) throws IOException {

        File file = file(scores.index(), scores.player());

        if(file.exists())
            throw new FileAlreadyExistsException("file already exist: " + file);

        try(FileChannel fc = FileChannel.open(file.toPath(), SAVE)) {
            ByteBuffer buffer = scores.scores;
            int size = scores.size();
            int count = 0;
            for(int pos=0; pos<size; ++pos) {
                int score = scores.getScore(pos);
                if(score!=0 || pos==size-1) {
                    buffer.position(pos);
                    int last = Math.min(pos|(BLOCK-1), size-1);
                    buffer.limit(last+1);
                    int n = fc.write(scores.scores, pos);
                    buffer.clear();
                    pos = last;
                    ++count;
                }
            }
            return count;
        }
    }

    public ScoreMap load(PosIndex index, Player player) throws IOException {

        File file = file(index, player);

        if(file.length() != index.range())
            throw new IOException("unexpected file size: " + file);

        try(FileChannel fc = FileChannel.open(file.toPath(), LOAD)) {
            int size = index.range();
            ByteBuffer scores = ByteBuffer.allocateDirect(size);
            size -= fc.read(scores);

            if(size!=0)
                throw new IOException("incomplete read: " + file);

            return new ScoreMap(index, player, scores);
        }
    }

    public ScoreMap map(PosIndex index, Player player, boolean readonly) throws IOException {

        File file = file(index, player);
        int size = index.range();

        if (file.length() != size)
            throw new IOException("unexpected file size: " + file);

        try(FileChannel fc = FileChannel.open(file.toPath(), readonly ? READ : WRITE)) {
            MappedByteBuffer scores = fc.map(readonly ? FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE, 0, size);
            return new ScoreMap(index, player, scores);
        }
    }
}
