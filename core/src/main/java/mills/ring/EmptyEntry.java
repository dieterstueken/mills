package mills.ring;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.07.24
 * Time: 20:53
 */
abstract public class EmptyEntry extends MinEntry {

    protected EmptyEntry() {
        super((short)0, (byte)0xff, (byte)0, (byte)0xff, (byte)0, new short[8]);
    }

    abstract public SingletonTable.Direct singleton();
}
