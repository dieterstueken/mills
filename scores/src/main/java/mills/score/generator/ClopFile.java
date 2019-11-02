package mills.score.generator;

import java.io.File;

public interface ClopFile extends IndexLayer {

    File file(String ext);

    default File file() {
        return file("scores");
    }
}
