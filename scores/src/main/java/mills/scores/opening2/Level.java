package mills.scores.opening2;

import mills.index.PosIndex;
import mills.position.Situation;
import mills.scores.ScoreFiles;
import mills.stones.MoveProcessor;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.01.14
 * Time: 15:46
 */
public class Level extends RecursiveTask<Level> {

    final ScoreFiles files;

    final int level;

    final List<HitMap> hitMaps;

    Level(ScoreFiles files, int level, List<HitMap> hitMaps) {
        this.files = files;
        this.level = level;
        this.hitMaps = hitMaps;
    }

    public String toString() {
        return String.format("Level %d [%d]", level, hitMaps.size());
    }

    public void stat(PrintStream out) {
        out.println(files.now());
        out.println(toString());

        for(HitMap hitMap:hitMaps) {
            out.println(hitMap.toString());
        }
        out.println();
    }

    static Level start(ScoreFiles files) {

        Situation start = Situation.start();
        PosIndex index = files.indexes.build(start.pop);
        HitMap empty = HitMap.full(start, index);

        return new Level(files, 0, Collections.singletonList(empty));
    }

    @Override
    protected Level compute() {

        if(level>=18)
            return null;

        // generate candidate hit maps
        HashSet<Situation> situations = new HashSet<Situation>() {
            @Override
            public boolean add(Situation situation) {
                // drop null values
                return situation != null && super.add(situation);
            }
        };

        for(HitMap hitMap:hitMaps) {
            if(hitMap.count==0)
                continue;

            Situation s = hitMap.situation;
            situations.add(s.put(false));
            situations.add(s.put(true));
        }

        // convert candidates into tasks
        List<ForkJoinTask<HitMap>> tasks = situations.stream().map(this::task).collect(Collectors.toList());

        invokeAll(tasks);

        // collect results
        List<HitMap> newMaps = tasks.stream().map((t) -> t.join()).filter(m -> m != null).collect(Collectors.toList());

        Level next = new Level(files, level+1, newMaps);

        next.fork();

        return next;
    }

    MoveProcessor processor(Situation s, boolean hit) {

        s = s.xput(hit);

        if(s==null)
            return MoveProcessor.NONE;

        for(HitMap hitMap:hitMaps) {
            if(hitMap.situation.equals(s))
                return hitMap.processor(hit);
        }

        return MoveProcessor.NONE;
    }

    ForkJoinTask<HitMap> task(Situation s) {

        /*
        if(s.taken()==0 && s.pop.max()<3)
            return new RecursiveTask<HitMap>() {

                @Override
                protected HitMap compute() {
                    PosIndex index = files.indexes.get(s.pop);
                    return HitMap.full(s, index);
                }
            };
        */

        PosIndex index = files.indexes.build(s.pop);

        MoveProcessor puts = processor(s, false);
        MoveProcessor hits = processor(s, true);

        return HitAnalyzer.create(s, index, puts, hits);
    }
}
