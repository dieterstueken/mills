package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.util.FutureReference;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public interface IndexProvider {

    default PosIndex build(PopCount pop) {
        return build(pop, null);
    }

    default PosIndex build(Clops clops) {
        return build(clops.pop(), clops.clop());
    }

    PosIndex build(PopCount pop, PopCount clop);

    static IndexProvider load() {
        ServiceLoader<IndexProvider> services = ServiceLoader.load(IndexProvider.class);
        return services.findFirst().orElse(null);
    }

    default IndexProvider lazy() {

        Map<Clops, Supplier<PosIndex>> suppliers = new ConcurrentHashMap<>();

        return new IndexProvider() {

            private PosIndex create(Clops clops) {
                return IndexProvider.this.build(clops);
            }

            private Supplier<PosIndex> lazy(Clops clops) {
                return FutureReference.of(()->create(clops));
            }

            @Override
            public PosIndex build(PopCount pop, PopCount clop) {
                return build(Clops.get(pop, clop));
            }

            @Override
            public PosIndex build(Clops clops) {
                return suppliers.computeIfAbsent(clops, this::lazy).get();
            }
            
            @Override
            public IndexProvider lazy() {
                return this;
            }
        };
    }
}
