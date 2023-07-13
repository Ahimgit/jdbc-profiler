package org.ard.jdbc.profiler.logging;

public class LoggerSlf4j implements Logger {

    private final org.slf4j.Logger logger;

    public LoggerSlf4j(final org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(final String category, final String message) {
        logger.info(String.format("[%1$s] %2$s", category, message));
    }

    @Override
    public void logSQL(final String category, final long elapsed, final String sql) {
        final StringBuilder msg = new StringBuilder();
        msg.append("[").append(category).append("]")
                .append(" time: ").append(elapsed);
        if (sql != null) {
            msg.append(", sql: ").append(sql);
        }
        logger.info(msg.toString());
    }

    @Override
    public void error(final String message, final Throwable th) {
        logger.info("[error] " + message, th);
    }

}
