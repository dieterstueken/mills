package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.11.19
 * Time: 21:57
 */
public interface Layer {

    PopCount pop();

    Player player();

    default boolean jumps() {
        return player().count(pop())<=3;
    }
}
