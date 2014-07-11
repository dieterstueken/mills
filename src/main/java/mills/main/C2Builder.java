package mills.main;

import com.google.common.collect.ImmutableList;
import mills.bits.BW;
import mills.bits.PopCount;
import mills.index.*;
import mills.position.Positions;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/6/14
 * Time: 7:10 PM
 */
public class C2Builder {

    final Function<List<RingEntry>, EntryTable> table_builder;

    public C2Builder(Function<List<RingEntry>, EntryTable> table_builder) {
        this.table_builder = table_builder;
    }

    public void add(RingEntry e2, RingEntry e0, RingEntry e1) {
        open2(e2);
        open0(e0);
        l1.add(e1);
    }

    RingEntry r0;
    final List<RingEntry> l0 = new ArrayList<>();
    final List<EntryTable> t1 = new ArrayList<>();
    final List<RingEntry> l1 = new ArrayList<>();

    void open0(RingEntry e0) {
        if(r0==null)
            r0 = e0;
        else
        if (!r0.equals(e0)) {
            if (!l1.isEmpty()) {
                EntryTable t = table_builder.apply(l1);
                t1.add(t);
                l1.clear();
                l0.add(r0);
            }

            r0 = e0;
        }
    }


    RingEntry r2;
    int index = 0;
    final List<R2Entry> l2 = new ArrayList<>(81);

    void open2(RingEntry e2) {
        if(r2==null)
            r2 = e2;
        else
        if(!r2.equals(e2)) {
            open0(null);

            if(!l0.isEmpty()) {
                EntryTable r0 = table_builder.apply(l0);
                R0Table t0 = R0Table.of(r0, ImmutableList.copyOf(t1));
                index += t0.range();
                R2Entry entry = new R2Entry(index, r2.index, t0);
                l2.add(entry);
                l0.clear();
                t1.clear();
            }

            r2 = e2;
        }
    }

    public List<R2Entry> build() {
        open2(null);
        index=0;
        return ImmutableList.copyOf(l2);
    }

    public static Map<PopCount, R2Index> build(R2Index index, Function<List<RingEntry>, EntryTable> table_builder) {

        final List<C2Builder> builders = new ArrayList<>(25);

        for(int i=0; i<25; ++i) {
            builders.add(new C2Builder(table_builder));
        }

        IndexProcessor processor = (posIndex, i201) -> {

            RingEntry r21 = Positions.r2(i201);
            RingEntry r01 = Positions.r0(i201);
            RingEntry r1 = Positions.r1(i201);

            PopCount clop = BW.clop(r21, r01, r1);
            C2Builder builder = builders.get(clop.hashCode());
            builder.add(r21, r01, r1);
        };

        index.process(processor);

        Map<PopCount, R2Index> indexMap = new HashMap<>(25);

        for(int nb=0; nb<5; ++nb)
            for(int nw=0; nw<5; ++nw) {
                PopCount clop = PopCount.of(nb, nw);
                C2Builder builder = builders.get(clop.hashCode());
                R2Index table = new R2Index(index.pop(), builder.build());
                indexMap.put(clop, table);
            }

        return indexMap;
    }

    public static void main(String ... args) {

        IndexList indexes = IndexList.create();

        for(PopCount pop:PopCount.TABLE) {
            R2Index posIndex = indexes.get(pop);
            int range = posIndex.range();
            int n20 = posIndex.values().size();

            System.out.format("%s %10d, %4d\n", pop, range, n20);

            Map<PopCount, R2Index> indexMap = build(posIndex, EntryTable::of);

            for (Map.Entry<PopCount, R2Index> entry : indexMap.entrySet()) {
                R2Index index = entry.getValue();
                range = index.range();
                if(range==0)
                    continue;

                n20 = index.values().size();
                PopCount clop = entry.getKey();

                System.out.format("\t%s %10d, %4d\n", clop, range, n20);
            }

            System.out.println();
        }
    }
}
