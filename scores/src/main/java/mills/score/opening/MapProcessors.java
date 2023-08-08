package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.position.Positions;
import mills.stones.Stones;
import mills.util.Indexer;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/*
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.08.23
 * Time: 14:44
 */

/**
 * Class MaProcessor processes a single OpeningMap.
 * For each possible target a MapActor delegates the data flow.
 */
public class MapProcessors implements Runnable {

    static final int CHUNK = 16*64;

    final OpeningMaps target;

    final Map<Clops, MapActor> actors = new ConcurrentSkipListMap<>(Indexer.INDEXED);

    MapProcessors(OpeningMaps target) {
        this.target = target;
    }


    MapActor getActor(Clops clops) {
        return actors.computeIfAbsent(clops, this::newActor);
    }

    private MapActor newActor(Clops clops) {
        return new MapActor(target.createMap(clops));
    }

    public void set(long i201) {
        Clops clops = Positions.clops(i201);
        processors.computeIfAbsent(clops);
    }

    private void processChunk(int chunk) {
        int start = chunk*CHUNK;
        source.index.process(this::process, start, start+CHUNK);
    }

    private void process(int posIndex, long i201) {
        Player player = source.player();
        int stay = Stones.stones(i201, player.opponent());
        int move = Stones.stones(i201, player);
        int free = Stones.STONES ^ (stay | move);
        int closes = Stones.closes(move);

        // place any free stone
        for (int j = free & -free; j != 0; free ^= j, j = free & -free) {
            int moved = move | j;

            if((closes&j)==0)
                process(stay, moved);
            else
                processClosed(stay, moved);
        }
    }

    private void process(int stay, int moved) {
        long m201 = Stones.i201(stay, moved, source.player());
        set(m201);
    }

    private void processClosed(int stay, int moved) {
        for (int m = stay, j = m & -m; j != 0; m ^= j, j = m & -m) {
            process(stay^j, moved);
        }
    }
}
