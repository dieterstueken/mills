package mills.score;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.position.Situation;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 20.02.23
 * Time: 15:02
 */
public class ScoreTree {

    final Map<Situation, Set<PopCount>> layers = new TreeMap<>();
    final NavigableSet<Situation> todo = new TreeSet<>();

    public ScoreTree() {
        clops(Situation.start()).add(PopCount.EMPTY);
        while(!todo.isEmpty())
            process(todo.pollFirst());
    }

    void process(Situation s) {
        if(s==null)
            return;

        process(s, s.next());

        System.out.format("%s:", s);
        clops(s).forEach(c->System.out.format(" %s", c));
        System.out.println();
    }

    void process(Situation s, Situation next) {
        Set<PopCount> clops = clops(s);

        // regular turn
        add(next, clops);

        // strokes

        // todo: may close or destroy 1 or 2 mills
        Player opp = s.player.opponent();
        PopCount px = next.pop.sub(opp.pop);
        Situation stroke = Situation.of(px, next.stock, opp);

        if(stroke!=null) {

            PopCount mclop = s.pop.mclop(false);
            clops.stream()
                    .map(c -> c.add(s.player.pop))
                    .filter(mclop::ge)
                    .forEach(c -> stroke(next, stroke, c));
        }
    }

    void stroke(Situation next, Situation stroke, PopCount clop) {
        add(stroke, clop);
        if(mayBeFrozen(next, clop))
            add(next, clop);
    }

    static boolean mayBeFrozen(Situation s, PopCount clop) {
        Player p = s.player.opponent();
        int n = p.count(s.pop);
        int c = p.count(clop);

        return switch (n) {
            case 3 -> c == 1;
            case 5, 6 -> c == 2;
            case 7 -> c == 3;
            case 8, 9 -> c == 3 || c == 4;
            default -> false;
        };
    }
    
    void add(Situation s, Collection<PopCount> clops) {
        for (PopCount clop : clops) {
            add(s, clop);
        }
    }

    //static final Situation DEBUG = Situation.of(PopCount.of(4,6), 7, Player.Black);

    boolean add(Situation s, PopCount clop) {
        if(s.stock==0 && s.pop.nb>s.pop.nw) {
            s = s.swap();
            clop = clop.swap();
        }

        Set<PopCount> clops = clops(s);
        boolean added = clops.add(clop);
        if(added && s.stock>0)
            added = true;

        return added;
    }

    Set<PopCount> clops(Situation s) {
        return layers.computeIfAbsent(s, this::compute);
    }

    Set<PopCount> compute(Situation s) {
        Set<PopCount> clops = new TreeSet<>();
        if(s.stock==0) {
            PopCount mclop = s.pop.mclop(true);
            PopCount.CLOPS.stream()
                    .filter(mclop::ge)
                    .forEach(clops::add);
        } else {
            if(s.stock+s.pop.sum()==18)
                clops.add(PopCount.EMPTY);
        }
        todo.add(s);

        return clops;
    }

    public static void main(String ... args) {
        new ScoreTree();
    }
}
