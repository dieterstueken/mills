package mills;

import mills.score.opening.Opening;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 22:39
 */
public class TestOpening {

    @Test
    public void testOpening() {
        double start = System.currentTimeMillis();
        try {
            new Opening().run();
        } catch(Throwable error) {
            error.printStackTrace();
        }
        double stop = System.currentTimeMillis();
        System.out.format("\n%.3fs\n", (stop - start) / 1000);
    }
}
