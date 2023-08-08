package mills.score.opening;

import mills.bits.Clops;
import mills.index.IndexProvider;
import mills.util.Indexer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static mills.score.opening.OpeningLayer.MAX_TURN;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 17:26
 *
 */
public class OpeningMaps {

    final IndexProvider provider;

    final int turn;

    final Map<Clops, OpeningMap> maps = new TreeMap<>(Indexer.INDEXED);

    public OpeningMaps(IndexProvider provider, int turn) {
        this.provider = provider;
        this.turn = turn;

        if(turn<0 || turn>MAX_TURN)
            throw new IndexOutOfBoundsException("invalid turn: " + turn);
    }

    public OpeningMaps(IndexProvider provider) {
        this(provider, 0);
        OpeningMap empty = OpeningMap.empty();
        maps.put(empty, empty);
    }

    public static OpeningMaps start(IndexProvider provider) {
        return new OpeningMaps(provider);
    }

    //static final Clops DEBUG = Clops.of(PopCount.of(2,3), PopCount.of(0,1));

    OpeningMap openMap(Clops clops) {
        //if(clops.equals(DEBUG))
        //    clops = clops;

        return maps.computeIfAbsent(clops, this::createMap);
    }

    OpeningMap createMap(Clops clops) {
        return OpeningMap.open(provider, turn, clops);
    }

    private MapProcessors processors(OpeningMap map) {
        return new MapProcessors(map, this);
    }

    public OpeningMaps next() {
        if(turn==MAX_TURN)
            return null;

        OpeningMaps next = new OpeningMaps(provider, turn+1);

        List<MapProcessors> processors = maps.values().stream().map(next::processors).toList();
        processors.parallelStream().forEach(MapProcessors::run);

        return next;
    }

    int complete() {
        maps.values().removeIf(OpeningMap::isEmpty);
        return (int) maps.values().parallelStream().filter(OpeningMap::isComplete).count();
    }

    private int count() {
        return maps.values().stream().mapToInt(OpeningMap::range).sum();
    }

    public void stat() {
        maps.values().stream().map(OpeningLayer::toString).forEach(System.out::println);
    }

    public static void main(String ... args) {

        IndexProvider provider = IndexProvider.load();

        long total = 0;
        double start = System.currentTimeMillis();

        for(OpeningMaps maps = start(provider); maps!=null; maps = maps.next()) {
            double stop = System.currentTimeMillis();
            double seconds = (stop - start) / 1000;
            int count = maps.count();
            total += count;
            int complete = maps.complete();
            maps.stat();
            System.out.format("turn: %d %d (%d) %,d %.3fs\n", maps.turn, maps.maps.size(), complete, count, seconds);
        }

        double stop = System.currentTimeMillis();

        System.out.format("total: %,d %.3fs\n", total, (stop - start) / 1000);
    }
}
