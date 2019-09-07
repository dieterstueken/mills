package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.09.19
 * Time: 15:42
 */
public interface Operation {

    int apply(int pattern);

    Operation invert();
}
