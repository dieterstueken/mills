package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.10.19
 * Time: 17:31
 */
public class Generator {

    final IndexProvider indexes;

    final File root;

    public Generator(IndexProvider indexes, File root) {
        this.indexes = indexes;
        this.root = root;
    }

    public void generateLevel(PopCount pop) {
        if(pop.min()<3)
            throw new IllegalArgumentException();

        Player player = pop.isEven() ? Player.White : Player.Black;
    }

    public void openLevel(PopCount pop) {


    }
}
