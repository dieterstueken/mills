package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;
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

    IndexBuilder builder = IndexBuilder.create();

    @Test
    public void mapped() throws IOException {
        PopCount pop = PopCount.of(5,5);
        PopCount clop = PopCount.of(0,0);
        PosIndex index = builder.build(pop, clop);

        File file = new File("output/p55-00.map");

        try(ScoreMap scores = ScoreMap.mapped(index, Player.White, file, false)) {
            index.process((idx, i201) ->{
                scores.setScore(idx, idx%13);
            });
        }
    }
}