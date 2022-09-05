package mills.score.opening;

import mills.bits.Player;
import mills.stones.Stones;
import mills.util.ConcurrentCompleter;

import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 16:33
 */
public class MapProcessor {

    static final int CHUNK = 16*64;

    // this is the player on Target
    final OpeningMap map;

    final LongPredicate source;

    final Player player;

    public MapProcessor(OpeningMap map, LongPredicate source) {
        this.map = map;
        this.source = source;
        this.player = map.player().opponent();
    }

    public void run() {
        int count = (map.index.range() + CHUNK - 1) / CHUNK;
        ConcurrentCompleter.compute(count, this::processChunk);
    }

    void processChunk(int chunk) {
        int start = chunk*CHUNK;
        map.index.process(this::process, start, start+CHUNK);
    }

    public void process(int posIndex, long i201) {
        if(analyze(i201))
            map.set(posIndex);
    }

    boolean analyze(long i201) {

        int stay = Stones.stones(i201, player.opponent());
        int move = Stones.stones(i201, player);
        int free = Stones.STONES ^ (stay|move);
        int closed = Stones.closed(move);

        for(int m=move, j=m&-m; j!=0; m^=j, j=m&-m) {
            int moved = move^j;

            if((closed&j)==0) {
                if(analyze(stay, moved))
                    return true;
            } else {
                if(analyzeClosed(stay, moved, free))
                    return true;
            }
        }

        // nothing hit.
        return false;
    }

    boolean analyze(int stay, int moved) {
        long i201 = Stones.i201(stay, moved, player);
        return source.test(i201);
    }

    boolean analyzeClosed(int removed, int moved, int free) {

        // closed mills after remove
        int closed = Stones.closed(removed);

        // some mill was broken
        boolean anyBroken = false;

        for(int j=free&-free; j!=0; free^=j, j=free&-free) {
            // j was taken
            int before = removed | j;

            // find if any mill was broken
            if(analyze(before, moved)) {
                // regular remove
                if((closed&j)==0)
                    return true;
                else
                    anyBroken = true;
            }
        }

        // report if any mill was broken.
        return anyBroken;
    }
}
