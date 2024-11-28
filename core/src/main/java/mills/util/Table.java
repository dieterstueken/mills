package mills.util;

import java.io.PrintStream;
import java.util.function.Function;

public interface Table<T> {

    T apply(int left, int right);

    static <T> Table<T> of(Table<T> f) {
        return f;
    }

    default <R> Table<R> map(Function<? super T, ? extends R> f, R nullValue) {
        return (left, right) -> {
            T value = apply(left, right);
            return value == null ? nullValue : f.apply(value);
        };
    }

    default <R> Table<R> map(Function<? super T, ? extends R> f) {
        return map(f, null);
    }

    default void dump(int size, String format) {
        dump(size, format, System.out);
    }

    default void dump(int size, String format, PrintStream out) {

        out.print("  ");
        for (int nb = 0; nb < size; nb++) {
            out.format(format, nb);
        }
        out.println();

        for (int nb = 0; nb < size; nb++) {
            out.format("%2d", nb);
            for (int nw = 0; nw < size; nw++) {
                T value = apply(nb, nw);
                if (value != null)
                    out.print(value);
            }

            out.println();
        }

        out.println();
    }
}