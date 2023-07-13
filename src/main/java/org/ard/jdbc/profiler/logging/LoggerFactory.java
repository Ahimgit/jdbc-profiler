package org.ard.jdbc.profiler.logging;

import java.util.HashMap;
import java.util.Map;

public interface LoggerFactory {

    Map<String, Logger> LOGGERS = new HashMap<>();

    default Logger getLogger(final String name) {
        Logger logger = LOGGERS.get(name);
        if (logger == null) {
            synchronized (LOGGERS) {
                logger = LOGGERS.get(name);
                if (logger == null) {
                    logger = createLogger(name);
                    LOGGERS.put(name, logger);
                }
            }
        }
        return logger;
    }

    Logger createLogger(String name);

}
