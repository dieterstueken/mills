package mills.score.opening;

import mills.bits.Clops;
import mills.index.IndexProvider;
import mills.position.Positions;
import mills.util.Indexer;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.LongPredicate;

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

    final Map<Clops, OpeningLayer> maps = new TreeMap<>(Indexer.INDEXED);

    public OpeningMaps(IndexProvider provider, int turn) {
        this.provider = provider;
        this.turn = turn;
    }

    public OpeningMaps(IndexProvider provider) {
        this(provider, 0);
        completeLayer(Clops.EMPTY);
    }

    public static OpeningMaps start(IndexProvider provider) {
        return new OpeningMaps(provider);
    }

    //static final Clops DEBUG = Clops.of(PopCount.of(2,3), PopCount.of(0,1));

    OpeningLayer openMap(Clops clops) {
        //if(clops.equals(DEBUG))
        //    clops = clops;

        return maps.computeIfAbsent(clops, this::createMap);
    }

    OpeningLayer completeLayer(Clops clops) {
        return maps.computeIfAbsent(clops, this::createLayer);
    }

    OpeningMap createMap(Clops clops) {
        return OpeningMap.open(provider, turn, clops);
    }

    OpeningLayer createLayer(Clops clops) {
        return new OpeningLayer(turn, clops);
    }

    public OpeningMaps next() {
        if(turn==MAX_TURN)
            return null;

        OpeningMaps next = new OpeningMaps(provider, turn+1);

        // setup finished target maps
        for (OpeningLayer layer : maps.values()) {
            if(layer.isComplete()) {
                Clops clops = layer.nextLayer();
                next.completeLayer(clops);
            }
        }

        // setup remaining layers
        for (OpeningLayer layer : maps.values()) {
            layer.nextLayers(next::openMap);
        }

        next.propagate(this::get);

        return next;
    }

    void propagate(LongPredicate source) {
        maps.values().parallelStream().forEach(layer->layer.propagate(source));
    }

    void reduce() {
        for (Clops clops : maps.keySet()) {
            maps.computeIfPresent(clops, (c,l)->l.reduce());
        }

        maps.values().removeIf(OpeningLayer::isEmpty);
    }

    boolean get(Long i201) {
        Clops clops = Positions.clops(i201);
        OpeningLayer map = maps.get(clops);

        if(map==null)
            return false;

        return map.get(i201);
    }

    private int count() {
        return maps.values().stream().mapToInt(OpeningLayer::range).sum();
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
            maps.stat();
            System.out.format("turn: %d %d %,d %.3fs\n", maps.turn, maps.maps.size(), count, seconds);
            maps.reduce();
        }

        double stop = System.currentTimeMillis();

        System.out.format("total: %,d %.3fs\n", total, (stop - start) / 1000);
    }

}
