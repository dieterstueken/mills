package mills.index.builder;

import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.util.CachedBuilder;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  02.03.2022 13:31
 * modified by: $
 * modified on: $
 */
public class IndexGroups implements IndexProvider {

    public interface Debug {

        default Reference<IndexGroup> newReference(IndexGroup value) {
            return new SoftReference<>(value);
        }

        default void start(PopCount pop) {}
        default void done(IndexGroup result) {}
    }


    static final Debug NOOP = new Debug() {};

    final Debug debug;

    final Partitions partitions = Partitions.create(ForkJoinPool.commonPool());

    final List<Provider> providers;

    public IndexGroups(Debug debug) {
        this.debug = debug;
        this.providers = PopCount.TABLE.transform(Provider::new).copyOf();
    }

    public IndexGroups() {
        this(NOOP);
    }

    public IndexGroup group(PopCount pop) {
        return provider(pop).get();
    }

    public Provider provider(PopCount pop) {
        return providers.get(pop.index);
    }

    public List<Provider> providers() {
        return providers;
    }

    public PosIndex build(PopCount pop, PopCount clop) {
        return group(pop).getIndex(clop);
    }

    public void close() {
        providers.forEach(CachedBuilder::clear);
    }

    public class Provider extends CachedBuilder<IndexGroup> {
        final PopCount pop;
        final boolean jump;

        // debug purpose
        Thread worker;

        Provider(final PopCount pop, boolean jump) {
            this.pop = pop;
            this.jump = jump;
        }

        Provider(final PopCount pop) {
            this(pop, false);
        }

        @Override
        public String toString() {
            return "IndexProvider(" + pop + ')';
        }

        @Override
        protected IndexGroup build() {
            debug.start(pop);
            worker = Thread.currentThread();
            GroupBuilder builder = GroupBuilder.create(partitions, pop, jump);
            IndexGroup result = new IndexGroup(pop, g->builder.build(g::newGroupIndex));
            debug.done(result);
            worker = null;
            return result;
        }

        @Override
        protected Reference<IndexGroup> newReference(final IndexGroup value) {
            return debug.newReference(value);
        }
    }
}
