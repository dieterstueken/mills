package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.score.Score;
import mills.util.ListSet;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class GroupGenerator verifies or generates missing score maps.
 */
class GroupsGenerator extends RecursiveAction {

    static final Logger LOGGER = Logger.getLogger(GroupsGenerator.class.getName());

    private static void log(Score score, int count) {
        LOGGER.log(Level.FINE, ()->String.format("%9s: %,9d", score, count));
    }

    final Generator generator;

    final PopCount pop;

    final ListSet<PopCount> clops;

    GroupsGenerator(Generator generator, PopCount pop) {
        this.generator = generator;
        this.pop = pop;
        this.clops = ListSet.of(MovingGroup.clops(pop).toArray(PopCount[]::new));
    }

    public String toString() {
        return String.format("%s[%d] %s", pop, clops.size(), !isDone() ? "Run" : cached.isEmpty()? "Done" : "Cached");
    }

    private Stream<Player> players() {
        if(pop.isSym())
            return Stream.of(Player.White);
        else
            return Stream.of(Player.White, Player.Black);
    }

    PosIndex index() {
        return generator.indexes.build(pop);
    }

    /**
     * Fork if not already done.
     * @return this generator for chaining.
     */
    public GroupsGenerator submit() {
        if(getForkJoinTaskTag()==0) {
            if(setForkJoinTaskTag((short) 1) != 1) {
                LOGGER.log(Level.FINE, ()->String.format("compute: %s", pop));
                this.fork();
            }
        }

        return this;
    }

    static <T> ForkJoinTask<T> submit(Callable<T> call) {
        return ForkJoinTask.adapt(call).fork();
    }

    @Override
    protected void compute() {

        if(exists())
            return;

        PosIndex index = index();

        ForkJoinTask<MovingGroups> whiteTask = submit(()->groups(index, Player.White));
        MovingGroups black = pop.isSym() ? whiteTask.join() : groups(index, Player.Black);
        MovingGroups white = whiteTask.join();

        // generate scores.
        generateScores(black, white);

        Stream<? extends TargetSlices> slices = white.moved.group.values().parallelStream();
        if (black != white)
            slices = Stream.concat(slices, black.moved.group.values().parallelStream());

        // finish all slice groups and save the scores.
        slices.peek(TargetSlices::close)
                .map(TargetSlices::scores)
                .forEach(generator::save);
    }

    MovingGroups groups(PosIndex index, Player player) {
        return GroupGenerator.create(this, index, player).groups();
    }

    boolean exists() {
        return players().allMatch(this::exists);
    }

    boolean exists(Player player) {
        for (PopCount clop : clops) {
            if(!generator.files.file(pop, clop, player).isFile())
                return false;
        }

        return true;
    }

    final Map<Player, Reference<LayerGroup<ScoreMap>>> cached = new EnumMap<>(Player.class);

    void clear() {
        int count = cached.size();
        if(count>0) {
            cached.values().forEach(Reference::clear);
            cached.clear();
            LOGGER.log(Level.INFO, String.format("cleared: %s %d", this, count));
        }
    }

    LayerGroup<ScoreMap> load(Player player) {

        if(pop.isSym() && player.equals(Player.Black))
            throw new IllegalArgumentException("Black player for " + pop);

        // wait if still computing.
        join();

        LayerGroup<ScoreMap> group = null;

        Reference<LayerGroup<ScoreMap>> ref = cached.get(player);
        if(ref!=null)
            group = ref.get();

        if(group==null) {
            PosIndex groups = generator.indexes.build(pop);
            Function<PopCount, ScoreMap> generate = clop -> generator.load(groups.getIndex(clop), player);
            Map<PopCount, ScoreMap> scores = LayerGroup.group(MovingGroup.clops(pop), generate);
            group = new LayerGroup<>(pop, player, scores);
            cached.put(player, new SoftReference<>(group));
            LOGGER.log(Level.INFO, String.format("load: %s", group));
        } else {
            LOGGER.log(Level.INFO, String.format("cached: %s", group));
        }

        return group;
    }

    private static void generateScores(MovingGroups self, MovingGroups other) {

        LOGGER.log(Level.FINE, ()->String.format("generate: %s <-> %s(%,d)", self.moved, other.moved, self.moved.range()));

        for (Score score = Score.LOST; true; score = score.next()) {

            IntStream tasks = self.propagate(other, score);
            if(self!=other)
                tasks = MovingGroups.concat(tasks, other.propagate(self, score));

            if (tasks == null)
                break;

            int count = tasks.parallel().sum();

            log(score, count);
        }
    }
}
