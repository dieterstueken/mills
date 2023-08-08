package mills.score.opening;

import mills.bits.Clops;
import mills.util.Indexer;

import java.util.*;
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
public class TargetProcessors implements AutoCloseable {

    final OpeningMaps target;

    final Map<Clops, MapActor> actors = new ConcurrentSkipListMap<>(Indexer.INDEXED);

    TargetProcessors(OpeningMaps target) {
        this.target = target;
    }

    MapActor getActor(Clops clops) {

        MapActor actor = actors.get(clops);

        if(actor==null) {
            synchronized (actors) {
                actor = actors.computeIfAbsent(clops, this::newActor);
            }
        }

        return actor;
    }

    private MapActor newActor(Clops clops) {
        return new MapActor(target.createMap(clops));
    }

    void process(OpeningMap source) {
        new MapProcessor(this, source).process();
    }

    void process(OpeningMaps source) {
        source.maps.values().parallelStream().forEach(this::process);
    }

    @Override
    public void close()  {
        actors.values().forEach(MapActor::close);
    }
}
