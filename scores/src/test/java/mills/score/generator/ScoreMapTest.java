package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.builder.IndexBuilder;
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

    ScoreFiles scores = new ScoreFiles(IndexBuilder.create(), new File("output"));

    @Test
    public void mapped() throws IOException {
        PopCount pop = PopCount.of(5,5);
        PopCount clop = PopCount.of(0,0);
        FileGroup group = scores.group(pop, Player.White, false);

        ScoreFile score = group.get(clop);

        try(ScoreMap scores = ScoreMap.mapped(score, false)) {
            scores.index().process((idx, i201) ->{
                scores.setScore(idx, idx%13);
            });
        }
    }
}