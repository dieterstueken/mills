package mills.index;

import mills.bits.PopCount;

import java.util.ServiceLoader;

public interface IndexProvider {

    default PosIndex build(PopCount pop) {
        return build(pop, null);
    }

    PosIndex build(PopCount pop, PopCount clop);

    static IndexProvider load() {
        ServiceLoader<IndexProvider> services = ServiceLoader.load(IndexProvider.class);
        return services.findFirst().orElse(null);
    }
}
