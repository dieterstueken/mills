package mills.index3.partitions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import mills.bits.PGroup;
import mills.bits.Pattern;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.Tasks;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:41
 * modified by: $Author$
 * modified on: $Date$
 */
public class Builder {

    final EntryTables registry;

    public Builder(EntryTables registry) {
        this.registry = registry;
    }

    public Partitions partitions() {

        List<MaskTable> partitions = Arrays.asList(new MaskTable[100]);

        List<MaskTable> content = new ArrayList<MaskTable>(45) {
            @Override
            public boolean add(MaskTable t) {
                partitions.set(t.pop().index, t);
                return super.add(t);
            }
        };

        Tasks.computeAll(PopCount.TABLE.stream()
                .filter(pop -> pop.sum()<=8), this::maskTable)
                .forEach(content::add);

        //System.out.format("radials: %d clops: %d\n", radials.get(), clops.get());


        return new Partitions(partitions, content);
    }

    EntryTable popTable(PopCount pop) {

        if(pop.sum()>8)
            return EntryTable.EMPTY;

        EntryTable table = RingEntry.MINIMIZED;
        table = table.filter(pop.eq);
        return registry.table(table);
    }

    MaskTable maskTable(PopCount pop) {

        EntryTable root = popTable(pop);

        if(root.isEmpty())
            return MaskTable.EMPTY;

        final Set<PGroup> groups = PGroup.groups(root);

        final List<ForkJoinTask<RadialTable>> taskset = new ArrayList<>(1<<groups.size());

        final List<ForkJoinTask<RadialTable>> tasks = new ArrayList<>(128);

        IntConsumer build = new IntConsumer() {

            ForkJoinTask<RadialTable> task(int index) {
                ForkJoinTask<RadialTable> task = Tasks.submit(() -> radialTable(root, index));
                taskset.add(task);
                return task;
            }

            @Override
            public void accept(int index) {
                int lindex = PGroup.lindex(groups, index);

                ForkJoinTask<RadialTable> task = lindex < index ? tasks.get(lindex) : task(index);
                tasks.add(index, task);
            }
        };

        IntStream.range(0, 128).forEach(build);

        EntryTable lePop = RingEntry.MINIMIZED.filter(pop.le);

        // invoke all and drop empty Radials
        Tasks.waitAll(taskset).removeIf(task -> task.join().root.isEmpty());

        assert !taskset.isEmpty();

        return maskTable(pop, root, lePop, Tasks.joinAll(tasks), Tasks.joinAll(taskset));
    }

    static MaskTable maskTable(PopCount pop, EntryTable root, EntryTable lePop, List<RadialTable> fragments, List<RadialTable> fragset) {

        return new MaskTable(root, lePop) {

            @Override
            public RadialTable get(int index) {
                return fragments.get(index);
            }

            @Override
            public List<RadialTable> content() {
                return fragset;
            }

            public PopCount pop() {
                return pop;
            }
        };
    }

    // clip off any radials not set
    static UnaryOperator<RingEntry> clip(Pattern blacks, Pattern whites) {
        return entry -> RingEntry.of(blacks.and(entry.b), whites.and(entry.w));
    }

    // create clip operator from given list of entries
    static UnaryOperator<RingEntry> clip(EntryTable table) {

        Pattern blacks = Pattern.NONE;
        Pattern whites = Pattern.NONE;

        for (RingEntry e : table) {

            e = e.radials(); // radials only

            blacks = blacks.or(e.b);
            whites = whites.or(e.w);
        }

        return clip(blacks, whites);
    }

    final List<Predicate<RingEntry>> filters = AbstractRandomList.generate(128, msk -> e -> e.stable(2 * msk));

    RadialTable radialTable(EntryTable source, int mlt) {
        EntryTable root = registry.table(source.filter(filters.get(mlt)));

        if(root.isEmpty())
            return RadialTable.EMPTY;

        final List<ForkJoinTask<ClopTable>> taskset = new ArrayList<>();
        final List<ForkJoinTask<ClopTable>> tasks = new ArrayList<>(81);

        RingEntry.RADIALS.stream().map(clip(root)).map(radials -> {
            int radix = radials.radix();
            int size = tasks.size();
            if (radix < size) {
                return tasks.get(radix);
            } else {
                assert radix == size;
                ForkJoinTask<ClopTable> task = Tasks.submit(() -> clopTable(root, radials));
                taskset.add(task);
                return task;
            }
        }).forEach(tasks::add);

        assert !taskset.isEmpty();

        Tasks.waitAll(taskset);
        List<ClopTable> fragset = Tasks.joinAll(taskset);
        List<ClopTable> tables = Tasks.joinAll(tasks);

        return radialTable(root, tables, fragset);
    }

    static RadialTable radialTable(EntryTable root, List<ClopTable> tables, Collection<ClopTable> fragset) {

        return tables.isEmpty() ? RadialTable.EMPTY : new RadialTable(root) {

            @Override
            public ClopTable get(int index) {
                return tables.get(index);
            }

            @Override
            public Collection<ClopTable> content() {
                return fragset;
            }
        };
    }

    ClopTable clopTable(EntryTable root, RingEntry radials) {

        if(root.isEmpty())
            return ClopTable.EMPTY;

        Map<PopCount, List<RingEntry>> clops = new TreeMap<>();

        for (RingEntry e: root) {
            PopCount clop = radials.and(e.radials()).pop().add(e.clop());

            if(clop.max()<=4)
                clops.computeIfAbsent(clop, c -> new ArrayList()).add(e);
        }

    /*    int keys[] = clops.entrySet().stream()
                .mapToInt(e -> e.getKey().index + 100*registry.key(e.getValue()))
                .toArray();
        */

        Map<PopCount, EntryTable> content = Maps.transformValues(clops, registry::table);
        content = ImmutableMap.copyOf(content);

        return ClopTable.of(content);
    }

    public static void main(String ... args) {

        Builder b = new Builder(new EntryTables());

        Partitions p = b.partitions();

        int n_rads=0, n_msk=0, n_clops=0, n_tables=0;

        for (MaskTable partition : p.content()) {

            if(!partition.content().isEmpty())
                ++n_msk;

            for (RadialTable radialTable : partition.content()) {
                if(!radialTable.content().isEmpty())
                    ++n_rads;

                for (ClopTable clopTable : radialTable.content()) {
                    if(!clopTable.content().isEmpty())
                        ++n_clops;

                    n_tables += clopTable.content().size();
                }
            }
        }

        //b.registry.stat(System.out);
        System.out.format("run msk=%d rad=%d clop=%d tbl=%d all=%d\n", n_msk, n_rads, n_clops, n_tables, b.registry.count());

        p.size();
    }
}
