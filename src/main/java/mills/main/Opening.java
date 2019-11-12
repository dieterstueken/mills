package mills.main;

import mills.bits.Player;
import mills.bits.PopCount;

import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.19
 * Time: 15:39
 */
public class Opening {

    public static void main(String ... args) {

        for(int n=0; n<18; ++n) {
            Player player = n%2==0 ? Player.White : Player.Black;
            int nb = n / 2;
            int nw = n - nb;
            PopCount pop = PopCount.get(nb, nw);
            process(player, pop);
        }
    }

    static void process(Player player, PopCount pop) {
        System.out.format("%d%d:%c\n", pop.nb, pop.nw, player.key());
        PopCount mclop = pop.mclop().min(pop.swap());
        forEach(mclop, taken -> taken(player, pop, taken));
    }

    static void taken(Player player, PopCount pop, PopCount taken) {

        PopCount board = pop.sub(taken.swap());
        PopCount mclop = board.mclop().min(taken.swap());
        forEach(mclop, closed -> closed(player, board, taken, closed));
    }

    static void closed(Player player, PopCount board, PopCount taken, PopCount closed) {
        System.out.format(" %d%d:%c-%d%d+%d%d\n",
                    board.nb, board.nw, player.key(),
                    taken.nb, taken.nw,
                    closed.nb, closed.nw);
    }

    static void forEach(PopCount pmax, Consumer<PopCount> action) {
        forEach(PopCount.EMPTY, pmax, action);
    }

    static void forEach(PopCount pop, PopCount pmax, Consumer<PopCount> action) {
        if(pmax.sub(pop)==null)
            return;
        action.accept(pop);
        forEach(pop.add(Player.White.pop), pmax, action);
        forEach(pop.add(Player.Black.pop), pmax, action);
    }
}
