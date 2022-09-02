package mills.score.opening;

import mills.bits.Clops;
import mills.index.IndexProvider;
import mills.position.Positions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 15:58
 */
public class MapTargets {

    final IndexProvider provider;

    final int turn;

    final Map<Clops, MapTarget> targets = new ConcurrentSkipListMap<>();

    MapTargets(IndexProvider provider, int turn) {
        this.provider = provider;
        this.turn = turn;

        Clops clops = OpeningLayer.clops(turn);
        MapTarget target = open(clops);

        targets.put(clops, target);
    }

    private MapTarget open(Clops clops) {
        OpeningLayer layer = new OpeningLayer(turn, clops);
        OpeningMap map = OpeningMap.open(provider, layer);
        return map.openTarget();
    }

    private void propagate(long i201) {
        Clops clops = Positions.clops(i201);
        var target = getTarget(clops);
        target.accept(i201);
    }

    public MapTarget getTarget(Clops clops) {
        return targets.computeIfAbsent(clops, this::open);
    }

    void process(OpeningMap map) {
        map.propagate(this::propagate);
    }

    public OpeningMaps close() {

        // close all targets
        targets.values().parallelStream().forEach(MapTarget::close);

        List<OpeningMap> maps = targets.values().stream().map(MapTarget::map).toList();
        return OpeningMaps.open(provider, turn, maps);
    }
}
