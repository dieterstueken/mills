package mills.position;

import mills.bits.Player;
import mills.bits.Sector;
import mills.ring.RingEntry;

import java.util.List;

import static mills.bits.Sector.E;
import static mills.bits.Sector.N;
import static mills.bits.Sector.NE;
import static mills.bits.Sector.NW;
import static mills.bits.Sector.S;
import static mills.bits.Sector.SE;
import static mills.bits.Sector.SW;
import static mills.bits.Sector.W;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  07.03.2021 18:45
 * modified by: $
 * modified on: $
 */
public class Board {

    static final String DUMMY = "◯━━●━━o";
    static final List<String> BOARD = List.of(
            "┏━━━━━━━━┳━━━━━━━━┓",
            "┃  ┏━━━━━╋━━━━━┓  ┃",
            "┃  ┃  ┏━━┻━━┓  ┃  ┃",
            "┣━━╋━━┫     ┣━━╋━━┫",
            "┃  ┃  ┗━━┳━━┛  ┃  ┃",
            "┃  ┗━━━━━╋━━━━━┛  ┃",
            "┗━━━━━━━━┻━━━━━━━━┛"
    );

    static final int K = 3; // stretch factor
    static final int M = 3; // center point

    static final int NY = BOARD.size(); // 7

    static final int NX = K*NY - K + 1; // 19

    static String board(long i201, Player player) {
        if(player==Player.Black)
            i201 = Positions.inverted(i201);

        StringBuilder sb = new StringBuilder();
        for(int iy=0; iy<NY; ++iy) {
            for (int ix = 0; ix < NX; ++ix)
                sb.append(get(ix, iy, i201));
            sb.append('\n');
        }

        return sb.toString();
    }

    static void show(long i201) {
        for(int iy=0; iy<NY; ++iy) {
            for (int ix = 0; ix < NX; ++ix)
                System.out.append(get(ix, iy, i201));
            System.out.println();
        }
    }

    static char get(int ix, int iy, long i201) {
        Player player = player(ix, iy, i201);

        if(player==Player.Black)
            return '●';

        if(player==Player.White)
            return '◯';

        return BOARD.get(iy).charAt(ix);
    }

    static Player player(int ix, int iy, long i201) {
        // odd values
        if((ix%K)!=0)
            return Player.None;
        ix /= K;

        ix -= M;
        iy -= M;

        int ir = Math.max(Math.abs(ix), Math.abs(iy));
        RingEntry e = ring(i201, ir);
        if(e==null)
            return Player.None;

        if((ix%ir)!=0 || (iy%ir)!=0)
            return Player.None;

        Sector s = sector(ix/ir, iy/ir);
        if(s==null)
            return Player.None;

        return e.player(s);
    }

    static Sector sector(int ix, int iy) {
        int k = (ix+1) + 10*(iy+1);
        return switch (k) {
            case 0 -> NW;
            case 1 -> N;
            case 2 -> NE;
            case 10 -> W;
            case 12 -> E;
            case 20 -> SW;
            case 21 -> S;
            case 22 -> SE;
            default -> null;
        };

    }

    static RingEntry ring(long i201, int ir) {

        return switch (ir) {
            case 1 -> Positions.r0(i201);
            case 2 -> Positions.r1(i201);
            case 3 -> Positions.r2(i201);
            default -> null;
        };

    }

    public static void main(String ... args) {

        RingEntry e0 = RingEntry.of(1234);
        RingEntry e2 = RingEntry.of(5514);
        RingEntry e1 = RingEntry.of(2456);

        long i201 = Positions.i201(e2, e0, e1, 0);
        show(i201);
    }
}
