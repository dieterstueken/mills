package mills.index.builder;

import mills.bits.PopCount;
import mills.util.ListMap;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.01.21
 * Time: 21:31
 */
public class Partitions extends ListMap<PopCount, Partition> {

    final List<Partition> values = partitions();
    
    public Partitions() {
        super(PopCount.TABLE, partitions());
    }

    public Partition get(PopCount pop) {
        return values.get(pop.index);
    }

    private static List<Partition> partitions() {
        Partition[] fragments = new Partition[PopCount.SIZE];
        Arrays.fill(fragments, Partition.EMPTY);

        PopCount.TABLE.stream().filter(pop->pop.sum()<=8)
                .parallel()
                .forEach(pop -> fragments[pop.index] = Partition.of(pop));

        return List.of(fragments);
    }

    public static void main(String ... args) {

        Partitions pt = new Partitions();

        for (int nb = 0; nb < 9; nb++) {
            for (int nw = 0; nw < 9; nw++) {
                final PopCount pop = PopCount.of(nb, nw);
                final Partition fs = pt.get(pop);

                System.out.format("%5d:%5d", fs.root.size(), fs.tables.count());
            }

            System.out.println();
        }

        System.out.println();
    }
}
