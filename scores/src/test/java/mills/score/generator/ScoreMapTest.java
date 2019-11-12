package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:50
 */
public class ScoreMapTest {

    ScoreFiles files = new ScoreFiles(IndexProvider.load(), new File("build/scores"));

    @Test
    public void mapped() throws IOException {
        PopCount pop = PopCount.of(5,5);
        PopCount clop = PopCount.of(0,0);
        FileGroup group = files.group(pop, Player.White, false);
        group.create();

        ScoreFile file = group.get(clop);

        try(ScoreMap scores = ScoreMap.mapped(file, false)) {
            scores.index().process((idx, i201) ->{
                scores.setScore(idx, idx%13);
            });
        }
    }

    @Test
    public void elevate() throws IOException {
        PopCount pop = PopCount.of(3,3);
        FileGroup group = files.group(pop, Player.White, false);

        try(SlicesGroup<MapSlice> slices = SlicesGroup.create(group)) {
            System.out.println("created");
        }
    }
}