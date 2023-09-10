package mills.score.opening;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.08.23
 * Time: 14:44
 */

/**
 * Class MaProcessors processes a set of source maps against a set of target maps.
 */

public class TargetProcessors implements AutoCloseable {

    final OpeningMaps source;

    final OpeningMaps target;

    AtomicInteger done = new AtomicInteger();

    TargetProcessors(OpeningMaps source, OpeningMaps target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public String toString() {
        return String.format("TargetProcessors(%d/%d)", done.get(), source.maps.size());
    }

    void process(OpeningMap source) {
        new TargetProcessor(target, source).process();
        done.incrementAndGet();
    }

    void process() {
        // forward all complete successors.
        source.maps.values().stream()
                .filter(OpeningMap::isComplete)
                .map(OpeningMap::nextClops)
                .map(target::openMap)
                .forEach(OpeningMap::setComplete);

        // process all maps
        source.maps.values().parallelStream().forEach(this::process);
    }

    @Override
    public void close()  {
        target.close();
    }
}
