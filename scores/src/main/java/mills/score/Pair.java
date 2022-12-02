package mills.score;

import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  12.12.12 18:45
 * modified by: $Author$
 * modified on: $Date$
 */
public class Pair<V> {

    public final V self;

    public final V other;

    public Pair(V self, V other) {
        this.self = self;
        this.other = other;
    }

    public Pair(Pair<? extends V> pair) {
        this(pair.self, pair.other);
    }

    public static <T> Pair<T> of(T self, T other) {
        return new Pair<>(self, other);
    }

    public String toString() {
        return String.format("<%s%s%s>", self, equal()?"-":",", other);
    }

    public Pair<V> swap() {
        if(equal())
            return this;
        else
            return of(other, self);
    }

    public static <T> Pair<T> join(ForkJoinTask<T> self, ForkJoinTask<T> other) {
        return new Pair<>(self.join(), other.join());
    }

    public boolean equal() {
        return Objects.equals(self, other);
    }

    public boolean all(Predicate<V> predicate) {
        return predicate.test(self) && (equal() || predicate.test(other));
    }

    public <T> Pair<T> map(Function<? super V,? extends T> map) {

        final T b = map.apply(self);
        final T w = equal() ? b : map.apply(other);

        return of(b, w);
    }

    public <T> Pair<T> mapParallel(Function<? super V,? extends T> map) {
        if(equal()) {
            T mapped = map.apply(self);
            return of(mapped, mapped);
        }

        ForkJoinTask<T> task = new RecursiveTask<T>() {
            @Override
            protected T compute() {
                return map.apply(other);
            }
        }.fork();

        return of(map.apply(self), task.join());
    }

    public <T> Pair<T> pair(Function<Pair<? super V>, ? extends T> f) {

        final T b = f.apply(this);
        final T w = equal() ? b : f.apply(swap());

        return of(b, w);
    }

    public <T> Pair<T> parallel1(Function<V, ? extends ForkJoinTask<T>> f) {

            ForkJoinTask<? extends T> t1 = f.apply(self);

            if(equal()) {
                T self = t1!=null ? t1.invoke() : null;
                return of(self, self);
            } else {
                if(t1!=null)
                    t1.fork();

                ForkJoinTask<? extends T> tx = f.apply(other);
                T other = tx!=null ? tx.invoke() : null;
                T self = (t1!=null) ? t1.join() : null;
                return of(self, other);
            }
        }

    public <T> Pair<T> parallel2(Function<Pair<V>, ? extends ForkJoinTask<T>> f) {

        ForkJoinTask<? extends T> t1 = f.apply(this);

        if(equal()) {
            T self = t1!=null ? t1.invoke() : null;
            return of(self, self);
        } else {
            if(t1!=null) t1.fork();

            ForkJoinTask<? extends T> tx = f.apply(swap());
            T other = tx!=null ? tx.invoke() : null;
            T self = (t1!=null) ? t1.join() : null;
            return of(self, other);
        }
    }

    public static <T> ForkJoinTask<Pair<T>> task(ForkJoinTask<T> self, ForkJoinTask<T> other) {
        return task(of(self, other));
    }

    public static <T> ForkJoinTask<Pair<T>> task(final Pair<ForkJoinTask<T>> tasks) {

       return new RecursiveTask<>() {
           @Override
           protected Pair<T> compute() {
               if (tasks.equal()) {
                   T self = tasks.self.invoke();
                   return of(self, self);
               } else {
                   tasks.other.fork();
                   T self = tasks.self.invoke();
                   T other = tasks.other.join();
                   return of(self, other);
               }
           }
       };
    }
}
