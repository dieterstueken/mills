package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.score.Score;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.01.20
 * Time: 18:01
 */
class GroupGenerator extends RecursiveTask<Map<Player, LayerGroup<ScoreMap>>> {

    static final Logger LOGGER = Logger.getLogger(GroupGenerator.class.getName());

    final Generator generator;

    final PopCount pop;

    final Set<PopCount> clops;

    GroupGenerator(Generator generator, PopCount pop) {
        this.generator = generator;
        this.pop = pop;
        this.clops = MovingGroup.clops(pop);

        LOGGER.log(Level.FINER, ()->String.format("generate: %s", pop));
    }

    private Stream<Player> players() {
        if(pop.isSym())
            return Stream.of(Player.White);
        else
            return Stream.of(Player.White, Player.Black);
    }

    public GroupGenerator submit() {
        if(getForkJoinTaskTag()==0) {
            if(setForkJoinTaskTag((short) 1) != 1)
                this.fork();
        }

        return this;
    }

    @Override
    protected Map<Player, LayerGroup<ScoreMap>> compute() {

        if(exists())
            return load();
        else
            return generate();
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

    Map<Player, LayerGroup<ScoreMap>> load() {
        EnumMap<Player, LayerGroup<ScoreMap>> result = new EnumMap<>(Player.class);

        ForkJoinTask.invokeAll(players().map(this::loadTask).collect(Collectors.toList()))
                .stream().map(ForkJoinTask::join)
                .forEach(group->result.put(group.player, group));

        return result;
    }

    ForkJoinTask<LayerGroup<ScoreMap>> loadTask(Player player) {
        return new RecursiveTask<>() {
            @Override
            protected LayerGroup<ScoreMap> compute() {
                return load(player);
            }
        };
    }

    LayerGroup<ScoreMap> load(Player player) {
        Stream<ScoreMap> scores = clops.parallelStream()
                .map(clop -> generator.indexes.build(pop, clop))
                .map(index -> generator.load(index, player));

        return new LayerGroup<>(pop, player, scores);
    }

    private void log(Score score, int count) {
        LOGGER.log(Level.FINER, ()->String.format("%9s: %9d", score, count));
    }

    private Stream<? extends ScoreMap> generate(MovingGroups self, MovingGroups other) {

        LOGGER.log(Level.FINER, ()->String.format("generate: %s (%d)", self.moved, self.moved.range()));

        for (Score score = Score.LOST; true; score = score.next()) {

            IntStream tasks = self.propagate(other, score);
            if(self!=other)
                tasks = MovingGroups.concat(tasks, other.propagate(self, score));

            if (tasks == null)
                break;

            int count = tasks.parallel().sum();

            log(score, count);
        }

        Stream<? extends MapSlices> slices = self.moved.group.values().parallelStream();
        if (other != self)
            slices = Stream.concat(slices, other.moved.group.values().parallelStream());

        return slices.peek(MapSlices::close)
                .map(MapSlices::scores);
    }

    Map<Player, LayerGroup<ScoreMap>> generate() {
        EnumMap<Player, MovingGroups> movings = new EnumMap<>(Player.class);

        ForkJoinTask.invokeAll(players()
                .map(this::groupTask).collect(Collectors.toList()))
                .stream().map(ForkJoinTask::join)
                .forEach(groups -> movings.put(groups.moved.player, groups));

        MovingGroups white = movings.get(Player.White);
        MovingGroups black = movings.getOrDefault(Player.Black, white);

        generate(black, white).forEach(generator::save);

        EnumMap<Player, LayerGroup<ScoreMap>> result = new EnumMap<>(Player.class);
        movings.forEach((player, group)->{
            Stream<ScoreMap> scoreMaps = group.moved.stream().map(MapSlices::scores);
            LayerGroup<ScoreMap> scores = new LayerGroup<>(pop, player, scoreMaps);
            result.put(player, scores);
        });

        return result;
    }

    ForkJoinTask<MovingGroups> groupTask(Player player) {
        // create task immediately since down group must be calculated synchronously
        ForkJoinTask<ClosingGroup<? extends ScoreSlices>> closingTask = generator.closingTask(pop, player);

        return new RecursiveTask<>() {
            @Override
            protected MovingGroups compute() {
                closingTask.fork();

                MovingGroup<MapSlices> moving = generator.moved(pop, player);

                return new MovingGroups(moving, closingTask.join());
            }
        };
    }
}
