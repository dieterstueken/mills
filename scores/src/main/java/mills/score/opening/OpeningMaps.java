package mills.score.opening;

import mills.bits.Clops;
import mills.bits.IClops;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.util.Indexer;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

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

    final Map<Clops, OpeningMap> maps = new ConcurrentSkipListMap<>(Indexer.INDEXED);

    public OpeningMaps(IndexProvider provider, int turn) {
        this.provider = provider;
        this.turn = turn;

        if(turn<0 || turn>MAX_TURN)
            throw new IndexOutOfBoundsException("invalid turn: " + turn);
    }

    public OpeningMaps(IndexProvider provider) {
        this(provider, 0);
        OpeningMap empty = OpeningMap.empty();
        maps.put(Clops.EMPTY, empty);
    }

    public static OpeningMaps start(IndexProvider provider) {
        return new OpeningMaps(provider);
    }

    //static final IClops DEBUG = IClops.of(PopCount.of(2,3), PopCount.of(0,1));

    OpeningMap openMap(Clops clops) {
        //if(clops.equals(DEBUG))
        //    clops = clops;

        OpeningMap map = maps.get(clops);
        if(map==null) {
            synchronized (clops) {
                PosIndex index = provider.build(clops);
                map = maps.computeIfAbsent(clops, c -> new OpeningMap(index, turn, false));
            }
        }

        return maps.computeIfAbsent(clops, this::createMap);
    }

    private OpeningMap createMap(IClops clops) {
        return OpeningMap.open(provider, turn, clops);
    }

    public OpeningMaps next() {
        if(turn==MAX_TURN)
            return null;

        OpeningMaps next = new OpeningMaps(provider, turn+1);

        try(TargetProcessors processors = new TargetProcessors(this, next)) {
            processors.process();
        }

        next.complete();

        return next;
    }

    int complete() {
        maps.values().removeIf(OpeningMap::isEmpty);
        long complete = maps.values().parallelStream().filter(OpeningMap::isComplete).count();
        return (int) complete;
    }

    private long count() {
        return maps.values().stream().mapToLong(OpeningMap::range).sum();
    }

    public void stat() {
        for (OpeningMap map : maps.values()) {
            map.isComplete();
            System.out.println(map);
        }

        //maps.values().stream().map(OpeningLayer::toString).forEach(System.out::println);
    }

    static class Stat {
        int count;

        long total;

        long partial;

        void stat(OpeningMap map) {
            ++count;

            if(map.isComplete())
                total += map.range();
            else
                partial += map.range();
        }

        void show() {
            System.out.format("count: %d complete: %,d partial: %,d\n", count, total, partial);
        }
    }

    public static void main(String ... args) {

        IndexProvider provider = IndexProvider.load();

        Stat stat = new Stat();
        double start = System.currentTimeMillis();

        for(OpeningMaps maps = start(provider); maps!=null; maps = maps.next()) {
            double stop = System.currentTimeMillis();
            double seconds = (stop - start) / 1000;
            int complete = maps.complete();
            System.out.format("turn: %d %d (%d) %.1fs\n",
                    maps.turn, maps.maps.size(), complete, seconds);


            Stat lstat = new Stat();
            maps.maps.values().forEach(lstat::stat);
            lstat.show();

            maps.maps.values().forEach(stat::stat);

            maps.stat();
            System.out.println();
        }

        double stop = System.currentTimeMillis();
        stat.show();
        
        System.out.format("elapsed: %.3fs\n",(stop - start) / 1000);
    }
}
