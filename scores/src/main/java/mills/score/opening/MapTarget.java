package mills.score.opening;

import mills.util.AbstractRandomList;
import mills.util.QueueActor;

import java.util.List;
import java.util.function.LongConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 16:22
 */
public class MapTarget implements LongConsumer, AutoCloseable {

    static final int CHUNK = Short.MAX_VALUE;

    OpeningMap map;

    final List<QueueActor<OpeningMap>> chunks;

    MapTarget(OpeningMap map) {
        this.map = map;

        int n = (map.index.range() + CHUNK - 1) / CHUNK;
        chunks = AbstractRandomList.virtual(n, i -> QueueActor.of(map)).copyOf();
    }

    @Override
    public void accept(long i201) {
        int posIndex = map.index.posIndex(i201);

        if (posIndex < 0)
            throw new IllegalArgumentException("position not found");

        int k = posIndex / CHUNK;
        chunks.get(k).submit(map -> map.set(posIndex));
    }

    public OpeningMap map() {
        return map;
    }

    @Override
    public void close() {
        chunks.forEach(QueueActor::close);
        map.stat();
    }
}
