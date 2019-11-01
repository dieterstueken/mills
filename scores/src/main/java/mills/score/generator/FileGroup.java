package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NotDirectoryException;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.10.19
 * Time: 15:57
 */
public class FileGroup extends Group<Function<String, File>> implements Layer {

    final File dir;

    final PopCount pop;

    final Player player;

    final boolean opening;

    private FileGroup(File dir, PopCount pop, Player player, boolean opening) {

        this.dir = dir;
        this.pop = pop;
        this.player = player;
        this.opening = opening;

        // maximum clop
        PopCount mclop = this.pop.mclop();

        // files are set up but not verified yet
        PopCount.CLOPS.forEach(clop->{
            if(mclop.le(clop))
                group.put(clop, file(clop));
        });
    }

    public PopCount pop() {
        return pop;
    }

    public Player player() {
        return player;
    }

    public boolean opening() {
        return opening;
    }

    boolean exists() {
        return dir.isDirectory();
    }

    public FileGroup swap() {
        PopCount swap = pop.swap();
        if(swap.equals(pop))
            return this;

        return new FileGroup(dir, swap, player, opening);
    }

    public FileGroup down() {
        PopCount down = pop.sub(player.other().pop);
        if(down==null || down.min()<3)
            return null;

        return new FileGroup(dir, down, player.other(), opening);
    }

    private Function<String, File> file(PopCount clop) {

        return ext -> {
            String name = name(clop, ext);
            return new File(dir, name);
        };
    }

    private String name(PopCount clop, String ext) {
        if(clop!=null)
            return String.format("%s%d%d+%d%d%c.%s", opening ? "O" : "P",
                    pop.nb(), pop.nw(),
                    clop.nb(), clop.nw(),
                    player.key(), ext);
        else
            return String.format("%s%d%d%c.%s", opening ? "O" : "P",
                pop.nb(), pop.nw(),
                player.key(), ext);
    }

    //////////////////////////////////////////////////

    public static FileGroup of(File root, PopCount pop, Player player, boolean opening)  {
        try {
            if(!root.exists())
                root.mkdirs();

            if(!root.isDirectory())
                throw new NotDirectoryException(root.toString());

            String name = String.format("%s%d%d%c", opening ? "O" : "P", pop.nb(), pop.nw(), player.key());
            File dir = new File(root, name);

            return new FileGroup(dir, pop, player, opening);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
