package beatchart;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.*;

import static java.util.logging.Level.INFO;

public class BeatchartLogger {

    /*
        SEVERE (highest)
        WARNING
        INFO
        CONFIG
        FINE
        FINER
        FINEST
     */
    public static Logger logger = Logger.getLogger("beatchart");

    static {
        ConsoleHandler handler = new ConsoleHandler();
        MyFormatter formatter = new MyFormatter();
        handler.setFormatter(formatter);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.setLevel(INFO);
    }
}

class MyFormatter extends Formatter {
    // KISS
    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getLevel()).append(": ");
        sb.append(record.getMessage()).append('\n');
        return sb.toString();
    }
}



