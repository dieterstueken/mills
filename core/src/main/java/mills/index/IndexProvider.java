package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;

import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

public interface IndexProvider extends AutoCloseable {

    PosIndex build(PopCount pop);

    default PosIndex build(Clops clops) {
        return this.build(clops.pop(), clops.clop());
    }

    default PosIndex build(PopCount pop, PopCount clop) {
        return this.build(pop).getIndex(clop);
    }

    default CompletableFuture<? extends PosIndex> stage(PopCount pop, PopCount clop) {
        return CompletableFuture.completedFuture(this.build(pop, clop));
    }

    default CompletableFuture<? extends PosIndex> stage(Clops clops) {
        return stage(clops.pop(), clops.clop());
    }

    default void close() {
    }

    static IndexProvider load() {
        ServiceLoader<IndexProvider> services = ServiceLoader.load(IndexProvider.class);
        return services.findFirst().orElse(null);
    }
}
