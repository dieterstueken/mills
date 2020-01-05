package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.score.Score;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

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

    public void generateLevel(PopCount pop) throws IOException {
        if(pop.min()<3)
            throw new IllegalArgumentException();

        if(pop.equals(pop.swap())) {
            generateSingle(pop);
        } else {
            generatePair(pop);
        }
    }

    private void generateSingle(PopCount pop) throws IOException {
        MovingGroups target = MovingGroups.create(pop, Player.White,
                clop -> moved(pop, clop, Player.White),
                clop -> closed(pop, clop, Player.White));

        System.out.format("%9s: %9d\n", target.moved, target.moved.range());

        Score score = Score.LOST;

        while(true) {
            int count = target.propagate(target, score);

            System.out.format("%9s: %9d\n", score, count);

            if(count==0)
                break;
            else
                score = score.next();
        }

        save(target.moved);
    }

    private void generatePair(PopCount pop) throws IOException {
        MovingGroups self = MovingGroups.create(pop, Player.White,
                clop -> moved(pop, clop, Player.White),
                clop -> closed(pop, clop, Player.White));

        MovingGroups other = MovingGroups.create(pop, Player.Black,
                clop -> moved(pop, clop, Player.Black),
                clop -> closed(pop, clop, Player.Black));

        System.out.format("%9s: %9d\n", self.moved, self.moved.range());

        Score score = Score.LOST;

        while(true) {
            int count = self.propagate(other, score);
            count += other.propagate(self, score);

            System.out.format("%9s: %9d\n", score, count);

            if(count==0)
                break;
            else
                score = score.next();
        }

        save(self.moved);
        save(other.moved);
    }

    private void save(MovingGroup<? extends MapSlices> moved) throws IOException {

        moved.group.values().parallelStream().forEach(ScoreSlices::close);

        for (MapSlices slices : moved.group.values()) {
            files.save(slices.scores());
        }
    }

    ScoreMap moved(PopCount pop, PopCount clop, Player player) {
        PosIndex index = indexes.build(pop, clop);
        ByteBuffer buffer = ByteBuffer.allocateDirect(index.range());
        return new ScoreMap(index, player, buffer);
    }

    ScoreSet closed(PopCount pop, PopCount clop, Player player) {
        PosIndex index = indexes.build(pop, clop);
        if(player.count(pop)<=3)
            return new LostSet(index, player);
        throw new IllegalStateException("not implemented");
    }

    public static void main(String ... args) throws IOException {
        int nb = Integer.parseInt(args[0]);
        int nw = Integer.parseInt(args[1]);
        PopCount pop = PopCount.get(nb, nw);

        Generator generator = new Generator(IndexProvider.load(), new File("build/scores"));
        generator.generateLevel(pop);

    }
}
