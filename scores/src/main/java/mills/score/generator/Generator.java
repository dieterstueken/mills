package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.10.19
 * Time: 17:31
 */
public class Generator {

    final IndexProvider indexes;

    final ScoreFiles files;

    public Generator(IndexProvider indexes, File root) throws IOException {
        this.indexes = indexes;
        this.files = new ScoreFiles(root);
    }

    private void generate(PopCount pop) {
        if (pop.min() < 3)
            throw new IllegalArgumentException();

        open(pop).generate().forEach(this::save);
    }

    GroupGenerator open(PopCount pop) {

        if(pop.isSym()) {
            MovingGroups self = groups(pop, Player.White);
            return new GroupGenerator(self, self);
        }

        ForkJoinTask<MovingGroups> task = new RecursiveTask<MovingGroups>() {
            @Override
            protected MovingGroups compute() {
                return groups(pop, Player.White);
            }
        }.fork();

        MovingGroups other = groups(pop, Player.Black);
        MovingGroups self = task.join();

        return new GroupGenerator(self, other);
    }

    MovingGroups groups(PopCount pop, Player player) {

        ClosingGroup<? extends ScoreSlices> closed = closed(pop, player);

        MovingGroup<MapSlices> moved = moved(pop, player);

        return new MovingGroups(moved, closed);
    }

    MovingGroup<MapSlices> moved(PopCount pop, Player player) {

        Stream<MapSlices> slices = MovingGroup.clops(pop).parallelStream()
                .map(clop -> indexes.build(pop, clop))
                .map(index -> ScoreMap.allocate(index, player))
                .map(MapSlices::of);

        return new MovingGroup<>(pop, player, slices);
    }

    ClosingGroup<? extends ScoreSlices> closed(PopCount pop, Player player) {
        if(player.count(pop)<=3)
            return ClosingGroup.lost(indexes, pop, player);

        PopCount down = pop.sub(player.pop);
        LayerGroup<ScoreMap> scores = load(down, down.isSym() ? Player.White : player);

        LayerGroup<IndexLayer> closed = new LayerGroup<>(pop, player,
                ClosingGroup.clops(pop, player).parallelStream()
                        .map(clop -> indexes.build(pop, clop))
                        .map(index -> IndexLayer.of(index, player))
        );

        return ClosingGroup.build(closed, scores);
    }
    
    LayerGroup<ScoreMap> load(PopCount pop, Player player) {

        Set<PopCount> clops = MovingGroup.clops(pop);

        for (PopCount clop : clops) {
            if(!files.file(pop, clop, player).isFile()) {
                generate(pop);
                break;
            }
        }

        Stream<ScoreMap> scores = clops.parallelStream()
                .map(clop -> indexes.build(pop, clop))
                .map(index -> load(index, player));

        return new LayerGroup<>(pop, player, scores);
    }

    ScoreMap load(PosIndex index, Player player) {
        try {
            return files.load(index, player);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void save(ScoreMap scores) {

        try {
            files.save(scores);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String ... args) throws IOException {
        int nb = Integer.parseInt(args[0]);
        int nw = Integer.parseInt(args[1]);
        PopCount pop = PopCount.get(nb, nw);

        File file = args.length<3 ? new File("build/scores") : new File(args[2]);
        IndexProvider indexes = IndexProvider.load().lazy();

        Generator generator = new Generator(indexes, file);

        generator.generate(pop);
    }
}
