package mills.score;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.05.13
 * Time: 18:56
 */
public class Diff {

    final MappedByteBuffer a;

    final MappedByteBuffer b;

    void run() {
        if(a.capacity()!=b.capacity())
            System.out.format("different sizes: %d %d\n", a.capacity(), b.capacity());
        else {
            int diff = 0;
            for(int pos=0; pos<a.capacity(); ++pos) {
                byte va = a.get(pos);
                byte vb = b.get(pos);
                if(va!=vb) {
                    System.out.format("%d: %d %d\n", pos, va, vb);
                    ++diff;
                }
            }
        }
    }

    public Diff(MappedByteBuffer a, MappedByteBuffer b) {
        this.a = a;
        this.b = b;
    }

    private static final OpenOption[] READ = new OpenOption[]{StandardOpenOption.READ};

    public static MappedByteBuffer open(String name) throws IOException {
        final File file = new File(name);
        final FileChannel fc = FileChannel.open(file.toPath(), READ);
        final int size = (int) fc.size();
        return fc.map(FileChannel.MapMode.READ_ONLY, 0, size);
    }

    public static void main(String ... args) throws IOException {
        if(args.length!=2)
            throw new IllegalArgumentException();

        new Diff(open(args[0]), open(args[1])).run();
    }
}
