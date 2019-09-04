package mills.scores;

import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.position.Situation;
import mills.util.QueueActor;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.12.12
 * Time: 16:34
 */
public class ScoreFiles {

    public final IndexProvider indexes = IndexProvider.load();

    final File root;

    static File root(String... args) {
        for (String arg : args)
            if (arg.startsWith("root="))
                return new File(arg.substring(5));

        return new File(".");
    }

    public ScoreFiles(String... args) {
        root = root(args);
    }

    public static String name(Situation situation, String ext) {

        if(situation.stock!=0)
            return String.format("o%02d+%d%d%c.%s", situation.stock,
                    situation.pop.nb, situation.pop.nw, situation.player.name().charAt(0), ext);
        else
            return String.format("m%d%d%c.%s",
                                situation.pop.nb, situation.pop.nw, situation.player.name().charAt(0), ext);
    }

    public IndexProvider indexes() {
        return indexes;
    }

    public File file(Situation situation, String ext) {
        return new File(root, name(situation, ext));
    }

    public static String name(final Situation situation) {
        return name(situation, "map");
    }

    private static final OpenOption READ[] = new OpenOption[]{StandardOpenOption.READ};
    private static final OpenOption WRITE[] = new OpenOption[]{StandardOpenOption.READ,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

    public boolean exists(Situation situation) {
        final String name = name(situation);
        final File file = new File(root, name);
        return file.exists();
    }

    public ScoreMap createMap(final Situation situation) {

        final File file = file(situation, "map");

        final PosIndex posIndex = indexes.get(situation.pop);
        final int size = posIndex.range();

        final ByteBuffer scores = ByteBuffer.allocateDirect(size);

        return new ScoreMap(scores, posIndex, situation) {

            @Override
            public void close() throws IOException {
                final FileChannel fc = FileChannel.open(file.toPath(), WRITE);
                fc.write(scores);
                fc.close();
            }
        };
    }

    public ScoreMap createMMap(final Situation situation) throws IOException {

        final File file = file(situation, "map");

        final PosIndex posIndex = indexes.get(situation.pop);
        final int size = posIndex.range();

        final FileChannel fc = FileChannel.open(file.toPath(), WRITE);
        final MappedByteBuffer scores = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);

        return new ScoreMap(scores, posIndex, situation) {

            @Override
            public void close() throws IOException {
                fc.close();
            }
        };
    }

    public ScoreMap loadMap(final Situation situation) throws IOException {
        ScoreMap scores = openMap(situation);

        if(scores!=null)
            scores.load();

        return scores;
    }

    @Nullable
    public ScoreMap openMap(final Situation situation) throws IOException {
        return openMap(situation, false);
    }

    public ScoreMap openMap(final Situation situation, boolean throwError) throws IOException {
        final File file = file(situation, "map");
        if (!file.exists()) {
            if(throwError)
                throw new FileNotFoundException(file.toString());
            return null;
        }

        final PosIndex posIndex = indexes.get(situation.pop);
        final int size = posIndex.range();

        try (FileChannel fc = FileChannel.open(file.toPath(), READ)) {

            //final ByteBuffer scores = ByteBuffer.allocateDirect(size);
            //fc.read(scores);

            final MappedByteBuffer scores = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);


            return new ScoreMap(scores, posIndex, situation);
        }
    }

    final DateFormat df = new SimpleDateFormat("HH:mm:ss");

    public String now() {
        return df.format(new Date());
    }

    QueueActor<ScoreFiles> queue = new QueueActor<>(this);

    public void print(String message) {
        queue.submit((t)->System.out.format("%s %s\n", t.now(), message));
    }
}
