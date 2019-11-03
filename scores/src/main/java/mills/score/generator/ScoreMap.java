package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;
import mills.score.Score;

import java.io.File;
import java.io.FileNotFoundException;
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

    protected final ByteBuffer scores;

    public ScoreMap(PosIndex index, Player player, boolean opening, ByteBuffer scores) {
        super(index, player, opening);
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

    MapSlice openSlice(int index) {
        return MapSlice.of(this, index);
    }

    Slices<MapSlice> slices() {
        return Slices.generate(this, this::openSlice);
    }

    ///////////////////////////////////////////////////////

    public static ScoreMap create(ScoreFile cf) {
        try {
            // return mapped(index, player, file, true);
            return _create(cf);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ScoreSet open(ScoreFile cf) {
        try {
            File file = cf.file("score");
            if(!file.exists())
                throw new FileNotFoundException(file.toString());
            return mapped(cf, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ScoreSet lost(IndexLayer layer) {
        return new ScoreSet(layer.index(), layer.player(), layer.opening()) {

            @Override
            public String toString() {
                return String.format("lost(%s)", layer.pop());
            }

            @Override
            public int getScore(int index) {
                return Score.LOST;
            }
        };
    }

    private static final OpenOption READ[] = new OpenOption[]{StandardOpenOption.READ};
    private static final OpenOption WRITE[] = new OpenOption[]{StandardOpenOption.READ,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

    private static ScoreMap _create(ScoreFile cf) throws IOException {

        File file = cf.file();
        if(file.exists())
            throw new FileAlreadyExistsException("file already exist: " + file.toString());

        PosIndex index = cf.index();
        int size = index.range();
        ByteBuffer scores = ByteBuffer.allocateDirect(size);

        return new ScoreMap(index, cf.player(), cf.opening(), scores) {

            @Override
            public String toString() {
                return file.getName();
            }
            
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

    public static ScoreMap mapped(ScoreFile cf, boolean readonly) throws IOException {
        PosIndex index = cf.index();
        int size = index.range();

        File file = readonly ? cf.file() : cf.file( "tmp");
        FileChannel fc = FileChannel.open(file.toPath(), readonly ? READ : WRITE);
        MappedByteBuffer scores = fc.map(readonly ? FileChannel.MapMode.READ_ONLY: FileChannel.MapMode.READ_WRITE, 0, size);

        return new ScoreMap(index, cf.player(), cf.opening(), scores) {

            @Override
            public String toString() {
                return file.getName();
            }

            @Override
            public void close() {
                try {
                    fc.close();
                    if(!readonly) {
                        file.renameTo(cf.file());
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }
}
