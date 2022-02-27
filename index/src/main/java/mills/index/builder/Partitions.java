package mills.index.builder;

import mills.bits.PopCount;
import mills.util.PopMap;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.01.21
 * Time: 21:31
 */
public class Partitions extends PopMap<Partition> {

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

        Partitions pts = new Partitions();

        pts.dump("root:", pt->String.format("%5d", pt.root.size()));
        pts.dump("max frag size:", pt-> {
            int max = 0;
            for (Fragments fm : pt.fragments) {
                max = Math.max(max, fm.root.size());
            }
            return String.format("%5d", pt.root.size());
        }
        );
        pts.dump("tables:", pt->String.format("%5d", pt.tables.count()));
    }
}
