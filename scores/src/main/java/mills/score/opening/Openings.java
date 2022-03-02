package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.index.IndexProvider;
import mills.position.Positions;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.IntStream;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  05.03.2021 19:45
 * modified by: $
 * modified on: $
 */
public class Openings {

    final IndexProvider indexes;

    final NavigableMap<OpeningLayer, CompletableFuture<? extends OpeningMap>> layers;

    public Openings(IndexProvider indexes) {
        this.indexes = indexes;
        layers = new ConcurrentSkipListMap<>(OpeningLayer.COMPARATOR);
        getMap(OpeningLayer.START).join().set(0);
    }

    CompletableFuture<? extends OpeningMap> getMap(OpeningLayer layer) {
        return layers.computeIfAbsent(layer, this::openNewMap);
    }

    private CompletableFuture<? extends OpeningMap> openNewMap(OpeningLayer layer) {
        return indexes
                .stage(layer.clops)
                .thenApply(index -> new OpeningMap(layer, index));
    }

    public void run() {
        for(int turn=0; turn<OpeningLayer.MAX_TURN; ++turn) {
            try(Turn t = new Turn(turn)) {
                Player player = turn%2==0 ? Player.White : Player.Black;
                System.out.format("turn: O%d%s %s %d\n", turn/2, player.key(), OpeningLayer.placed(turn), t.input.size());
                t.run();
            }
        }

        System.out.format("total: %2d\n", layers.size());
    }

    static final int CHUNK = Short.MAX_VALUE;

    class Turn implements AutoCloseable {

        final int turn;

        final NavigableMap<OpeningLayer, CompletableFuture<? extends OpeningMap.Target>> targets;

        final List<? extends OpeningMap> input;

        Turn(int turn) {
            this.turn = turn;

            this.targets = new ConcurrentSkipListMap<>(OpeningLayer.COMPARATOR);

            this.input = layers.values().stream().map(CompletableFuture::join)
                    .filter(om -> om.layer.turn == turn).toList();
        }

        OpeningMap.Target getTarget(Clops clops) {
            OpeningLayer layer = new OpeningLayer(turn+1, clops);
            return targets.computeIfAbsent(layer, this::openNewTarget).join();
        }

        CompletableFuture<? extends OpeningMap.Target> openNewTarget(OpeningLayer layer) {
            return getMap(layer).thenApply(OpeningMap::openTarget);
        }

        public void run() {
            input.parallelStream().forEach(this::propagate);
        }

        public void close() {
            targets.values().stream()
                    .map(CompletableFuture::join)
                    .forEach(OpeningMap.Target::close);
        }

        void propagate(OpeningMap map) {
            int chunks = (map.index.range() + CHUNK - 1) / CHUNK;
            IntStream.range(0, chunks).parallel().forEach(chunk -> new Processor(map).processChunk(chunk));
        }

        class Processor {

            final OpeningMap map;

            final Mover mover;
            final Mover closer;

            Processor(OpeningMap map) {
                this.map = map;
                this.mover = Moves.TAKE.mover(map.layer.player() == Player.Black);
                this.closer = Moves.TAKE.mover(map.layer.player() != Player.Black);
            }

            void processChunk(int chunk) {
                map.index.process(this::process, CHUNK * chunk, CHUNK * (chunk + 1));
            }

            public void process(int posIndex, long i201) {
                Player player = map.layer.player();
                int stay = Stones.stones(i201, player.other());
                int move = Stones.stones(i201, player);
                int mask = Stones.STONES ^ (stay | move);

                mover.move(stay, move, mask);
                mover.normalize();
                mover.analyze(this::propagate1);
            }

            private void propagate1(long i201) {
                Clops clops = Positions.clops(i201);

                if(!clops.clop().equals(map.layer.clop())) {
                    // take an opponents stone
                    Player player = map.layer.player();
                    int self = Stones.stones(i201, player);
                    int oppo = Stones.stones(i201, player.other());
                    int closed = Stones.closed(oppo);

                    // any non mill stones?
                    if(closed!=oppo)
                        closer.move(self, oppo, oppo^closed);
                    else
                        closer.move(self, oppo, closed);

                    closer.normalize();
                    closer.analyze(this::propagate2);
                } else
                    propagate2(i201);
            }

            private void propagate2(long i201) {
                Clops clops = Positions.clops(i201);
                var target = getTarget(clops);
                target.accept(i201);
            }
        }
    }
    
    public static void main(String ... args) {
        double start = System.currentTimeMillis();

        new Openings(IndexProvider.load()).run();
        double stop = System.currentTimeMillis();

        System.out.format("total: %.3fs\n", (stop - start) / 1000);
    }
}
