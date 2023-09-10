package mills.bits;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.09.23
 * Time: 11:58
 */
class ClopsTest {

    @Test
    void index() {
        int total = 0;
        for (PopCount clop : PopCount.CLOPS) {
            List<PopCount> pops = PopCount.TABLE.stream().filter(p -> p.mclop().ge(clop)).toList();
            total += pops.size();
            System.out.format("%s: %2d\n", clop, pops.size());
        }

        System.out.format("total: %d\n", total);
    }
}