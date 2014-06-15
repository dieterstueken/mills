package mills.scores.opening;

import mills.bits.Player;
import mills.index.IndexList;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Situation;
import mills.scores.ScoreSlice;
import mills.stones.Stones;
import mills.util.AbstractRandomArray;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  16.10.13 12:58
 * modified by: $Author$
 * modified on: $Date$
 */
public class CountOpen {

    public static void main(String... args) {
        new CountOpen().run();
    }

    final IndexList indexes = IndexList.create();

    final Map<Situation, AtomicBoolean> done = new ConcurrentHashMap<>();

    boolean done(Situation s) {
        return done.computeIfAbsent(s, key -> new AtomicBoolean(false)).getAndSet(true);
    }

    void run() {
        ForkJoinPool.commonPool().submit(() -> {
            play(Situation.start(), "");
        }).join();
    }

    boolean play(Situation s, String hits) {

        if (s == null || !s.popStock().valid() || done(s)) {
            //System.out.format("%-6s: done   %s\n", s, hits);
            return false;
        }

        //System.out.format("%-6s: start %s\n", s, hits);

        ForkJoinTask<Boolean> hit = count(s, hits);
        hit.fork();

        if (s.stock > 1) {
            Situation put = s.put(false);

            if (hit.join()) {
                String hitx = hits + (put.player == Player.White ? "O" : "X");
                play(put.hit(put.player), hitx);
            }

            play(put, hits);
        }

        hit.join();

        return true;
    }

    ForkJoinTask<Boolean> count(Situation s, String hits) {

        return new RecursiveTask<Boolean>() {

            final AtomicInteger putMax = new AtomicInteger();
            final AtomicInteger hitMax = new AtomicInteger();
            final AtomicInteger done = new AtomicInteger();

            @Override
            protected Boolean compute() {

                PosIndex index = indexes.get(s.pop);

                List<RecursiveAction> slices = slices(index);
                invokeAll(slices);

                System.out.format("%-7s: %3d %3d %s\n", s, putMax.get(), hitMax.get(), hits);

                return hitMax.get() > 0;
            }

            List<RecursiveAction> slices(PosIndex index) {
                int size = ScoreSlice.sliceCount(index);

                //return IntStream.range(0, size).mapToObj(i -> count(index, i)).collect(Collectors.toList());

                return new AbstractRandomArray<RecursiveAction>(size) {

                    @Override
                    public RecursiveAction get(int i) {
                        int start = i * Slice.BLOCK;
                        return count(index, start);
                    }
                }.immutableCopy();
            }

            RecursiveAction count(PosIndex posIndex, int i) {

                return new RecursiveAction() {
                    @Override
                    protected void compute() {
                        int start = i * Slice.SIZE;
                        int size = posIndex.size();
                        int end = Math.min(start + Slice.SIZE, size);
                        Count count = new Count(s);
                        posIndex.process(count, start, end);

                        putMax.accumulateAndGet(count.putMax, Math::max);
                        hitMax.accumulateAndGet(count.hitMax, Math::max);
                        int i = done.incrementAndGet();

                        //int n = ScoreSlice.sliceCount(posIndex);
                        //System.out.format("%s %d/%d done %d - %d\n", s, i, n, start, end);
                    }
                };
            }
        };
    }

    static class Count implements IndexProcessor {

        int putMax = 0;
        int hitMax = 0;

        final Situation situation;
        final PutStone<?> put;
        final PutStone<?> hit;

        Count(Situation situation) {
            this.situation = situation;
            put = PutStone.put(situation, false);
            hit = PutStone.hit(situation, false);
        }

        @Override
        public void process(int posIndex, long i201) {

            final Player player = situation.player;
            final int black = Stones.stones(i201, player.other());
            final int white = Stones.stones(i201, player);
            final int free = Stones.free(black | white);
            final int closes = free & Stones.closes(white);

            put.move(black, white, free ^ closes);

            if (put.size() > putMax)
                putMax = put.size();

            if (closes != 0)
                hit.move(black, white, closes);
            if (hit.size() > hitMax)
                hitMax = hit.size();
        }
    }
}
