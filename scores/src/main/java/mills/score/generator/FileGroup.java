package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NotDirectoryException;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.10.19
 * Time: 15:57
 */
public class FileGroup extends LayerGroup<ScoreFile> {

    final IndexProvider indexes;

    final File dir;

    private FileGroup(IndexProvider indexes, File dir, Layer layer) {
        this(indexes, dir, layer.pop(), layer.player(), layer.opening());
    }

    private FileGroup(IndexProvider indexes, File dir, PopCount pop, Player player, boolean opening) {
        super(pop, player, opening);
        this.indexes = indexes;
        this.dir = dir;
    }

    FileGroup populate() {
        return populate(null);
    }

    FileGroup populate(Predicate<PopCount> filter) {
        // maximum clop
        PopCount mclop = this.pop.mclop();

        // files are set up but not verified yet
        PopCount.CLOPS.forEach(clop->{
            if(filter==null || filter.test(clop))
                if(mclop.le(clop))
                    group.put(clop, file(clop));
        });

        return this;
    }

    boolean exists() {
        return dir.isDirectory();
    }

    public FileGroup swap() {
        PopCount swapped = pop.swap();
        if(swapped.equals(pop))
            return this;

        return new FileGroup(indexes, dir, swapped, player, opening).populate();
    }

    public FileGroup down() {
        PopCount down = pop.sub(player.other().pop);
        if(down==null || down.min()<3)
            return null;

        // drop group of non closed
        return new FileGroup(indexes, dir, down, player.other(), opening)
                .populate(clop->!PopCount.EMPTY.equals(clop));
    }

    private ScoreFile file(PopCount clop) {

        return new ScoreFile() {

            @Override
            public PopCount pop() {
                return pop;
            }

            @Override
            public Player player() {
                return player;
            }

            @Override
            public boolean opening() {
                return opening;
            }

            @Override
            public PopCount clop() {
                return clop;
            }

            @Override
            public File file(String ext) {
                String name = name(clop, ext);
                return new File(dir, name);
            }

            @Override
            public PosIndex index() {
                return indexes.build(pop(), clop());
            }
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

    public static FileGroup of(IndexProvider indexes, File root, PopCount pop, Player player, boolean opening)  {
        try {
            if(!root.exists())
                root.mkdirs();

            if(!root.isDirectory())
                throw new NotDirectoryException(root.toString());

            String name = String.format("%s%d%d%c", opening ? "O" : "P", pop.nb(), pop.nw(), player.key());
            File dir = new File(root, name);

            return new FileGroup(indexes, dir, pop, player, opening).populate();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
