package mills.score.opening;

import mills.bits.Clops;
import mills.util.Indexer;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    final OpeningMaps source;

    final OpeningMaps target;

    final Map<Clops, MapActor> actors = new ConcurrentSkipListMap<>(Indexer.INDEXED);

    AtomicInteger done = new AtomicInteger();

    TargetProcessors(OpeningMaps source, OpeningMaps target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public String toString() {
        return String.format("TargetProcessors(%d/%d)", done.get(), source.maps.size());
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
        return MapActor.open(target.openMap(clops));
    }

    void process(OpeningMap source) {
        new MapProcessor(this, source).process();
        done.incrementAndGet();
    }

    void process() {
        // forward all complete next maps
        source.maps.values().stream()
                .filter(OpeningMap::isComplete)
                .map(OpeningMap::nextClops)
                .map(target::openMap)
                .forEach(OpeningMap::complete);

        // process all maps
        source.maps.values().parallelStream().forEach(this::process);
    }

    @Override
    public void close()  {
        actors.values().forEach(MapActor::close);
    }
}
