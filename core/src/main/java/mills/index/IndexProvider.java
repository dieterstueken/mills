package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface IndexProvider extends AutoCloseable {

    default PosIndex build(PopCount pop) {
        return this.build(pop, null);
    }

    default PosIndex build(Clops clops) {
        return this.build(clops.pop(), clops.clop());
    }

    PosIndex build(PopCount pop, PopCount clop);

    Map<PopCount, ? extends PosIndex> buildGroup(PopCount pop);

    default CompletionStage<? extends PosIndex> stage(PopCount pop, PopCount clop) {
        return CompletableFuture.completedStage(this.build(pop, clop));
    }

    default CompletionStage<? extends PosIndex> stage(Clops clops) {
        return stage(clops.pop(), clops.clop());
    }

    default void close() {
    }

    static IndexProvider load() {
        ServiceLoader<IndexProvider> services = ServiceLoader.load(IndexProvider.class);
        return services.findFirst().orElse(null);
    }
}
