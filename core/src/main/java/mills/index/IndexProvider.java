package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
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

        return new IndexProvider() {

            Map<Clops, Supplier<PosIndex>> suppliers = new ConcurrentHashMap<>();

            @Override
            public PosIndex build(PopCount pop, PopCount clop) {
                return build(Clops.get(pop, clop));
            }

            @Override
            public PosIndex build(Clops clops) {
                return suppliers.computeIfAbsent(clops, this::supplier).get();
            }
            
            private Supplier<PosIndex> supplier(Clops clops) {

                return () -> {

                    RecursiveTask<PosIndex> task = new RecursiveTask<>() {
                        @Override
                        protected PosIndex compute() {
                            return IndexProvider.this.build(clops);
                        }
                    };

                    AtomicBoolean started = new AtomicBoolean(false);

                    if(!started.getAndSet(true))
                        task.fork();

                    return task.join();
                };
            }
        };
    }
}
