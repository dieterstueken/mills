package mills.position;

import mills.bits.Player;
import mills.bits.Sector;
import mills.ring.Entries;
import mills.ring.RingEntry;

import java.util.List;

import static mills.bits.Sector.*;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  07.03.2021 18:45
 * modified by: $
 * modified on: $
 */
public class Board {

    static final List<String> BOARD = List.of(
            "┏━━━━━┳━━━━━┓",
            "┃ ┏━━━╋━━━┓ ┃",
            "┃ ┃ ┏━┻━┓ ┃ ┃",
            "┣━╋━┫   ┣━╋━┫",
            "┃ ┃ ┗━┳━┛ ┃ ┃",
            "┃ ┗━━━╋━━━┛ ┃",
            "┗━━━━━┻━━━━━┛"
    );

    static void show(long i201) {
        for(int iy=0; iy<7; ++iy) {
            for (int ix = 0; ix < 13; ++ix)
                System.out.append(get(ix, iy, i201));
            System.out.println();
        }
    }

    static char get(int ix, int iy, long i201) {
        Player player = player(ix, iy, i201);

        if(player==Player.Black)
            return '●';

        if(player==Player.White)
            return 'o';

        return BOARD.get(iy).charAt(ix);
    }

    static Player player(int ix, int iy, long i201) {
        if((ix&1)!=0)
            return Player.None;

        ix = ix/2-3;
        iy = 3-iy;

        int ir = Math.max(Math.abs(ix), Math.abs(iy));
        RingEntry e = ring(i201, ir);
        if(e==null)
            return Player.None;

        if((ix%ir)!=0 || (iy%ir)!=0)
            return Player.None;

        Sector s = sector(ix/ir, ix/ir);
        if(s==null)
            return Player.None;

        return e.player(s);
    }

    static Sector sector(int ix, int iy) {
        int k = (ix+1) + 10*(iy+1);
        switch(k) {
            case 0: return NW;
            case 1: return N;
            case 2: return NE;
            case 10: return W;
            case 12: return E;
            case 20: return SW;
            case 21: return S;
            case 22: return SE;
        }

        return null;
    }

    static RingEntry ring(long i201, int ir) {

        switch(ir) {
            case 1: return Positions.r0(i201);
            case 2: return Positions.r1(i201);
            case 3: return Positions.r2(i201);
        }

        return null;
    }

    public static void main(String ... args) {

        RingEntry e0 = Entries.of(1234);
        RingEntry e2 = Entries.of(5514);
        RingEntry e1 = Entries.of(2456);

        long i201 = Positions.i201(e2, e0, e1, 0);
        show(i201);
    }
}
