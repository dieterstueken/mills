package mills.score.opening;

import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.List;

import static mills.score.opening.OpeningLayer.MAX_TURN;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 17:26
 */
public class OpeningMaps {

    final IndexProvider provider;

    final int turn;

    final List<OpeningMap> maps;

    public OpeningMaps(IndexProvider provider, int turn, List<OpeningMap> maps) {
        this.provider = provider;
        this.turn = turn;
        this.maps = maps;
    }
    
    public static OpeningMaps open(IndexProvider provider, int turn, List<OpeningMap> maps) {
        return new OpeningMaps(provider, turn, maps);
    }

    public static OpeningMaps start(IndexProvider provider) {
        PosIndex index = provider.build(OpeningLayer.START);
        OpeningMap empty = new OpeningMap(index, OpeningLayer.START);
        empty.set(0);
        return open(provider, 0, List.of(empty));
    }

    public OpeningMaps next() {
        if(turn==MAX_TURN)
            return null;

        MapTargets targets = new MapTargets(provider, turn+1);
        maps.parallelStream().forEach(targets::process);
        return targets.close();
    }

    public static void main(String ... args) {
        double start = System.currentTimeMillis();

        IndexProvider provider = IndexProvider.load();

        for(OpeningMaps maps = start(provider); maps!=null; maps = maps.next()) {
            double stop = System.currentTimeMillis();
            System.out.format("turn: %d %d %.3fs\n", maps.turn, maps.maps.size(), (stop - start) / 1000);
        }

        double stop = System.currentTimeMillis();

        System.out.format("total: %.3fs\n", (stop - start) / 1000);
    }
}
