package mills.position;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 22.12.12
 * Time: 14:43
 */
public class Situation implements Position.Factory {

    public final PopCount pop;

    public final Player player;

    public final int stock;

    public static Situation of(PopCount pop, int stock, Player player) {

        assert stock >= 0 && stock <= 18 : "invalid stock";
        assert player != Player.None && player != null : "no player";

        if (pop == null)   // null transparent
            return null;

        // no stones taken
        Situation full = OPENINGS.get(stock);

        // use directly
        if (full.pop.equals(pop) && full.player == player)
            return full;

        assert pop.sum() + stock <= 18 : "too many stones";

        return new Situation(pop, stock, player);
    }

    public static Situation of(PopCount pop, Player player) {
        return of(pop, 0, player);
    }

    public Player player() {
        return player;
    }

    public PopCount pop() {
        return pop;
    }

    public String toString() {
        if (stock == 0)
            return String.format("%d%d%c", pop.nb, pop.nw, player.name().charAt(0));
        else
            return String.format("%02d+%d%d%c", stock, pop.nb, pop.nw, player.name().charAt(0));
    }

    public Situation swap() {
        return new Situation(pop.swap(), stock, player.other());
    }

    public PopCount popMax() {
        Situation full = OPENINGS.get(stock);
        return full.pop.swapIf(full.player != player);
    }

    public PopCount popTaken() {
        return popMax().sub(pop);
    }

    public PopCount popStock() {
        return PopCount.of(9, 9).sub(popTaken());
    }

    public int taken() {
        return 18 - stock - pop.sum();
    }

    public Situation hit(Player who) {
        PopCount hit = pop.sub(who.pop);
        return Situation.of(hit, stock, player);
    }

    /**
     * Put a new stone from stock and possibly hit some opponents stone.
     *
     * @param hit if some opponents stone to take away.
     * @return the new Situation or null if this action is impossible.
     */
    public Situation put(boolean hit) {

        if (stock <= 0)
            return null;

        PopCount put = pop.add(player.pop);

        if (hit) {
            // take a stone
            put = put.sub(player.other().pop);
            if (put == null)
                return null;

            // compare stone taken to stones set
            PopCount full = OPENINGS.get(stock - 1).pop();

            int take = full.sub(put).count(player.other());
            int set = full.count(player);

            // impossible since too few mills to close
            if (set <= 2 * take)
                return null;
        }

        return Situation.of(put, stock - 1, player.other());
    }

    /**
     * Opposite/reverse action of put().
     *
     * @param hit if some opponents stone was taken away.
     * @return the new Situation or null if this action is impossible.
     */
    public Situation xput(boolean hit) {

        if (stock >= 18)
            return null;

        // reverse move
        PopCount xput = pop.sub(player.other().pop);

        // may cause underflow
        if (xput == null)
            return null;

        if (hit) {
            // todo: compare stone taken to stones set

            // all stones already on board, too many stones needed.
            if (xput.sum() + stock + 1 >= 18)
                return null;

            xput = xput.add(player.pop);
        }

        return Situation.of(xput, stock + 1, player.other());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Situation s = (Situation) o;

        return (stock == s.stock)
                && Objects.equals(player, s.player)
                && Objects.equals(pop, s.pop);
    }

    @Override
    public int hashCode() {
        int result = pop.hashCode();
        result += 100 * stock;
        result += 10000 * player.ordinal();
        return result;
    }

    @Override
    public Position position(long i201) {

        return new Position(i201) {

            @Override
            public StringBuilder format(StringBuilder sb) {
                sb.append(Situation.this.toString()).append(" / ");
                sb = super.format(sb);
                return sb;
            }
        };
    }

    private Situation(PopCount pop, int stock, Player player) {
        this.pop = pop;
        this.player = player;
        this.stock = stock;
    }

    public static final List<Situation> OPENINGS = openings();

    public static Situation start() {
        return OPENINGS.get(18);
    }

    private static List<Situation> openings() {

        return AbstractRandomList.generate(19, stock -> {
            int step = 18 - stock;
            int nb = step / 2;
            int nw = (step + 1) / 2;
            PopCount pop = PopCount.of(nb, nw);
            Player player = (step % 2) == 0 ? Player.White : Player.Black;
            return new Situation(pop, stock, player);
        });
    }
}
