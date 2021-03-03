package mills.index.builder;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.tables.C2Table;
import mills.util.AbstractRandomList;
import mills.util.CachedEntry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletionStage;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  01.03.2021 17:19
 * modified by: $
 * modified on: $
 */
public class CachedBuilder implements IndexProvider {

    final List<CachedEntry<C2Table>> cache;

    public CachedBuilder(IndexBuilder builder) {
        this.cache = AbstractRandomList
                .transform(Clops.CLOPS, clop -> new CachedEntry<>(()->builder.build(clop)))
                .copyOf();
    }

    public CachedBuilder() {
        this(new IndexBuilder());
    }

    private CachedEntry<C2Table> entry(PopCount pop, PopCount clop) {
        return cache.get(Clops.index(pop, clop));
    }

    @Override
    public C2Table build(PopCount pop, PopCount clop) {
        return entry(pop, clop).get();
    }

    @Override
    public CompletionStage<C2Table> stage(PopCount pop, PopCount clop) {
        return entry(pop, clop).future();
    }

    @Override
    public Map<PopCount, C2Table> buildGroup(PopCount pop) {

        C2Table[] tables = new C2Table[PopCount.CLOPS.size()];

        PopCount.CLOPS.parallelStream()
                .map(clop->build(pop, clop))
                .filter(Objects::nonNull)
                .forEach(table -> tables[table.clop().index]=table);

        Map<PopCount, C2Table> group = new TreeMap<>();
        for (C2Table table : tables) {
            if(table!=null)
                group.put(table.clop(), table);
        }

        return group;
    }

    @Override
    public void close() {
        cache.forEach(CachedEntry::clear);
    }
}
