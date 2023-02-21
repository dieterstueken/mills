package mills.score;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.GroupIndex;
import mills.index.PosIndex;
import mills.util.PopMap;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.02.23
 * Time: 14:19
 */
public class ScoreGroup implements AutoCloseable {

    final File root;

    final PopMap<ScoreMap> maps;

    ScoreGroup(GroupIndex index, File root, boolean create) {
        this.root = root;

        List<ScoreMap> scores = index.group().values().stream().map(ix->open(ix, create)).toList();

        this.maps = PopMap.of(index.group().keySet(), scores);
    }

    public Player player() {
        return Player.White;
    }

    public ScoreMap get(PopCount clop) {
        return maps.get(clop);
    }

    ScoreMap open(PosIndex index, boolean create) {
        try {
            String name = String.format("m%sc%s%s.map", index.pop(), index.clop(), player());
            File file = new File(root, name);

            if (create)
                return wopen(index, file);
            else
                return ropen(index, file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    ScoreMap ropen(PosIndex index, File file) throws IOException {

        int size = index.range();

        FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        MappedByteBuffer scores = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);

        return new ScoreMap(scores, index, player());
    }

    ScoreMap wopen(PosIndex index, File file) throws IOException {

        if(file.exists())
            throw new IOException("output file already exists: " + file.getName());

        int size = index.range();
        ByteBuffer scores = ByteBuffer.allocateDirect(size);

        return new ScoreMap(scores, index, player()) {
            @Override
            public void close() {
                super.close();
                try {
                    FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                    fc.write(scores);
                    fc.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    @Override
    public void close() {
        maps.values().forEach(ScoreMap::close);
    }
}
