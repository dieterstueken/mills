package mills.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/22/15
 * Time: 6:25 PM
 */
public class Tasks {

    public static <T> ForkJoinTask<T> submit(Callable<T> compute) {
        return ForkJoinTask.adapt(compute).fork();
    }

    public static <T, R> Function<T, ForkJoinTask<R>> submit(Function<T,R> compute) {
        return t -> submit(() -> compute.apply(t));
    }

    public static <T> List<T> joinAll(List<? extends ForkJoinTask<T>> tasks) {
        return AbstractRandomList.map(tasks, ForkJoinTask::join);
    }

    public static <T, R> Stream<R> computeAll(Stream<T> input, Function<T,R> compute) {
        Deque<ForkJoinTask<R>> tasks = new ArrayDeque<>();

        input.map(submit(compute)).forEach(tasks::addFirst);

        return tasks.stream().map(ForkJoinTask::join);
    }

    public static <T, R> List<R> computeAll(Collection<T> src, Function<? super T, R> compute) {

        return joinAll(src.stream().
                map(t -> submit(() -> compute.apply(t))).
                collect(Collectors.toList()));
    }

    public static <T> List<? extends ForkJoinTask<T>> waitAll(List<? extends ForkJoinTask<T>> tasks) {

        // use reverse order
        for(int i=tasks.size(); i>0; --i)
            tasks.get(i-1).join();

        return tasks;
    }
}
