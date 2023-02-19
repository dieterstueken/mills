package mills.score;

import mills.bits.Player;
import mills.index.PosIndex;
import mills.util.PopMap;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    File root;
    Player player;

    final PopMap<ScoreMap> maps;

    ScoreGroup(PopMap<? extends PosIndex> group, Player player, File root, boolean create) {
        this.root = root;
        this.player = player;

        List<ScoreMap> list = group.values().stream().map(index -> open(index, create)).toList();

        this.maps = PopMap.of(group.keySet(), list);
    }

    ScoreMap open(PosIndex index, boolean create) {
        try {
            if (create)
                return wopen(index);
            else
                return ropen(index);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    ScoreMap ropen(PosIndex index) throws IOException {
        String name = String.format("m%sc%s%s.map", index.pop(), index.clop(), player);
        File map = new File(root, name);

        int size = index.range();

        FileChannel fc = FileChannel.open(map.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        MappedByteBuffer scores = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);

        return new ScoreMap(scores, index, player);
    }

    ScoreMap wopen(PosIndex index) throws IOException {

        String name = String.format("m%sc%s%s.map", index.pop(), index.clop(), player);
        File map = new File(root, name);
        if(map.exists())
            throw new IOException("output file already exists: " + name);

        name = String.format("m%sc%s%s.tmp", index.pop(), index.clop(), player);
        File tmp = new File(root, name);

        int size = index.range();

        FileChannel fc = FileChannel.open(tmp.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        MappedByteBuffer scores = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);

        for(int i=0; i<size; i+=4096) {
            scores.put(i, (byte)0);
        }

        scores.force();

        return new ScoreMap(scores, index, player) {
            @Override
            public void close() {
                super.close();
                try {
                    fc.close();
                    if (!tmp.renameTo(map))
                        throw new IOException("failed to rename to " + map.getName());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    @Override
    public void close() throws Exception {
        maps.values().forEach(ScoreMap::close);
    }
}
