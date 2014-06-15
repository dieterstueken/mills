package mills.scores.opening2;

import mills.bits.Player;
import mills.index.PosIndex;
import mills.position.Positions;
import mills.position.Situation;
import mills.stones.MoveProcessor;
import mills.stones.MoveTable;
import mills.stones.Stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.01.14
 * Time: 19:28
 */

/**
 * Class HitMap is the result of an analysis of hits for a given situation.
 */
public class HitMap {

    final Situation situation;

    final PosIndex index;

    final BitMap hits;

    final int count;

    public HitMap(Situation situation, PosIndex index, BitMap hits, int count) {
        this.situation = situation;
        this.index = index;
        this.hits = hits;
        this.count = count;
    }

    public MoveProcessor processor(boolean hit) {
        return hit ? processXHit() : processXPut();
    }

    MoveProcessor processXHit() {

        // 1. any closed mill?
        // 2. put back taken stone (from/to mill?)
        // 3. perform xput

        return new MoveProcessor() {

            final MoveProcessor processMoved = processMoved();

            @Override
            public boolean process(int stay, int move) {

                // closed mill pattern after stone put
                int mclosed = Stones.closed(move);
                if(mclosed==0)
                    return false;

                // remember stones taken from a closed mill.
                boolean anyClosed = false;

                // closed pattern "after" stone hit
                int sclosed = Stones.closed(stay);

                // stones to put pack the hit opponent
                int mask = Stones.STONES ^ (stay|move);

                for(int m=1; m<=mask; m<<=1) {
                    if((mask&m)!=0) {

                        int xstay = stay|m;

                        int n = MoveTable.TAKE.move(xstay, move, mclosed, processMoved);

                        if(n<0) {
                            int xclosed = Stones.closed(xstay);

                            // was not taken from a closed mill
                            if(xclosed==sclosed)
                                return true;

                            // taken from a closed mill
                            anyClosed = true;
                        }
                    }
                }

                return anyClosed;
            }
        };
    }

    MoveProcessor processXPut() {

        return new MoveProcessor() {

            final MoveProcessor processMoved = processMoved();

            @Override
            public boolean process(int stay, int move) {

                int closed = Stones.closed(move);

                // move non mill stones
                int n = MoveTable.TAKE.move(stay, move, move^closed, processMoved);

                // indicates processMoved == true
                return n<0;
            }
        };
    }

    MoveProcessor processMoved() {

        // process after put stone was removed
        return new MoveProcessor() {

            final Positions.Builder builder = situation.player== Player.White ? Positions.BW : Positions.WB;

            @Override
            public boolean process(int stay, int move) {
                long i201 = builder.i201(stay, move);
                int posIndex = index.posIndex(i201);
                boolean hit = hits.get(posIndex);
                return hit;
            }
        };
    }

    public static HitMap full(Situation situation, PosIndex index) {
        return new HitMap(situation, index, BitMap.FULL, index.size()) {
            @Override
            public MoveProcessor processMoved() {
                return MoveProcessor.ANY;
            }

            public String toString() {
                return String.format("HitMap %s -%s full %d", situation, situation.popTaken(), index.size());
            }
        };
    }

    public static HitMap empty(Situation situation, PosIndex index) {
        return new HitMap(situation, index, BitMap.EMPTY, index.size()) {
            @Override
            public MoveProcessor processMoved() {
                return MoveProcessor.NONE;
            }

            public String toString() {
                return String.format("HitMap %s -%s empty", situation, situation.popTaken());
            }
        };
    }

    public String toString() {

        double p2 = count==0 ? -1 : (Math.log(index.size() / (double)count)/Math.log(2.0));

        return String.format("HitMap %s -%s %d/%d (%.1f)", situation, situation.popTaken(), count, index.size(), p2);
    }
}
