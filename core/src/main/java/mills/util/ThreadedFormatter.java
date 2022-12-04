package mills.util;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.01.20
 * Time: 18:35
 */
public class ThreadedFormatter extends SimpleFormatter {

    @Override
    public String formatMessage(LogRecord record) {
        String message = super.formatMessage(record);
        return String.format("[%d] %s", record.getLongThreadID(), message);
    }
}
