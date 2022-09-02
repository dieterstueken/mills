package mills.index.builder;

import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.CachedBuilder;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  02.03.2022 13:31
 * modified by: $
 * modified on: $
 */
abstract public class AbstractGroupBuilder implements IndexProvider {

    public interface Debug {

        default Reference<IndexGroup> newReference(IndexGroup value) {
            return new SoftReference<>(value);
        }

        default void start(PopCount pop) {}
        default void done(IndexGroup result) {}
    }

    static final Debug NOOP = new Debug() {};

    final Debug debug;

    final ForkJoinPool pool = new ForkJoinPool();

    final Partitions partitions;
    final PopMap<EntryTable> lePops;
    final PopMap<EntryTable> minPops;

    final List<IndexBuilder> builders;

    public AbstractGroupBuilder(Debug debug) {
        this.debug = debug;

        this.lePops = PopMap.lePops(Entries.TABLE);
        this.minPops = PopMap.lePops(Entries.MINIMIZED);

        var task = ForkJoinTask.adapt(Partitions::new).fork();
        this.partitions = task.join();

        this.builders = PopCount.TABLE.transform(this::newEntry).copyOf();
    }


    public IndexGroup group(PopCount pop) {
        return entry(pop).get();
    }

    public IndexBuilder entry(PopCount pop) {
        return builders.get(pop.index);
    }

    public List<IndexBuilder> builders() {
        return builders;
    }

    public PosIndex build(PopCount pop, PopCount clop) {
        return group(pop).getIndex(clop);
    }

    public void close() {
        builders.forEach(CachedBuilder::clear);
    }

    private IndexBuilder newEntry(PopCount pop) {
        return new IndexBuilder(pop) {

            @Override
            protected IndexGroup build() {
                return buildGroup(pop);
            }
        };
    }


    abstract protected IndexGroup buildGroup(PopCount pop);
}
