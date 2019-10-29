package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.10.19
 * Time: 15:57
 */
public class FileGroup {

    final String prefix;

    final PopCount pop;

    final Player player;

    final File dir;

    private FileGroup(File dir, String prefix, PopCount pop, Player player) {
        this.prefix = prefix;
        this.pop = pop;
        this.player = player;
        this.dir = dir;
    }

    public static FileGroup create(File root, String prefix, PopCount pop, Player player)  {
        try {
            String name = String.format("%s%d%d%c", prefix, pop.nb(), pop.nw(), player.key());
            File dir = new File(root, name);

            if(!dir.exists())
                dir.mkdirs();

            if(!dir.isDirectory())
                throw new NotDirectoryException(dir.toString());

            return new FileGroup(dir, prefix, pop, player);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Map<PopCount, File> group(Predicate<PopCount> clops) {

        Map<PopCount, File> group = new HashMap<>();

        for (PopCount clop : PopCount.CLOSED) {
            if(clops.test(clop))
                group.put(clop, file(clop));
        }

        return group;
    }

    public File file(PopCount clop) {
        if(clop==null)
            return file();

        String name = String.format("%s%d%d+%d%d%c", prefix,
                pop.nb(), pop.nw(),
                clop.nb(), clop.nw(),
                player.key());
        return new File(dir, name);
    }

    public File file() {
        String name = String.format("%s%d%d%c", prefix,
                pop.nb(), pop.nw(),
                player.key());
        return new File(dir, name);
    }
}
