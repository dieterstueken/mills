package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.10.19
 * Time: 17:52
 */
abstract public class ScoreMap extends ScoreSet {

    private final ByteBuffer scores;

    public ScoreMap(PosIndex index, Player player, ByteBuffer scores) {
        super(index, player);
        this.scores = scores;
    }

    @Override
    public int getScore(int index) {
        int value = scores.get(index);
        value &= 0xff;  // clip off sign bit

        return value;
    }

    public void setScore(int posIndex, int score) {
        byte value = (byte) (score&0xff);
        scores.put(posIndex, value);
    }

    ///////////////////////////////////////////////////////

    public static ScoreMap open(PosIndex index, FileGroup files, boolean create) {
        return open(index, files.player, files.file(index.clop()), create);
    }

    static ScoreMap open(PosIndex index, Player player, File file, boolean create) {
        if(create)
            return create(index, player, file);
        else
            return open(index, player, file);
    }

    public static ScoreMap create(PosIndex index, Player player, File file) {
        try {
            // return mapped(index, player, file, true);
            return _create(index, player, file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ScoreMap open(PosIndex index, Player player, File file) {
        try {
            return mapped(index, player, file, false);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final OpenOption READ[] = new OpenOption[]{StandardOpenOption.READ};
    private static final OpenOption WRITE[] = new OpenOption[]{StandardOpenOption.READ,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

    private static ScoreMap _create(PosIndex index, Player player, File file) throws IOException {

        if(file.exists())
            throw new FileAlreadyExistsException("file already exist: " + file.toString());

        int size = index.range();
        final ByteBuffer scores = ByteBuffer.allocateDirect(size);

        return new ScoreMap(index, player, scores) {
            @Override
            public void close() {
                try {
                    final FileChannel fc = FileChannel.open(file.toPath(), WRITE);
                    fc.write(scores);
                    fc.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    public static ScoreMap mapped(PosIndex index, Player player, File file, boolean readonly) throws IOException {
        final int size = index.range();

        final FileChannel fc = FileChannel.open(file.toPath(), readonly ? READ : WRITE);
        final MappedByteBuffer scores = fc.map(readonly ? FileChannel.MapMode.READ_ONLY: FileChannel.MapMode.READ_WRITE, 0, size);

        return new ScoreMap(index, player, scores) {
            @Override
            public void close() {
                try {
                    fc.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }
}
