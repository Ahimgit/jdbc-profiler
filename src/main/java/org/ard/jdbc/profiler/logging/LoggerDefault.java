package org.ard.jdbc.profiler.logging;

import java.io.PrintStream;
import java.util.Date;

public class LoggerDefault implements Logger {

    private final PrintStream stream;
    public LoggerDefault(final PrintStream stream) {
        this.stream = stream;
    }

    public void log(final String category, final String message) {
        stream.format("%1$tF %1$tT.%1$tL %2$s [%3$s] %4$s%n",
                new Date(),
                Thread.currentThread().getName(),
                category, message);
    }

    public void logSQL(final String category, final long elapsed, final String sql) {
        final StringBuilder msg = new StringBuilder();
        msg.append("time: ").append(elapsed);
        if (sql != null) {
            msg.append(", sql: ").append(sql);
        }
        log(category, msg.toString());
    }

    public void error(final String message, final Throwable th) {
        log("error", message);
        if (th != null) {
            th.printStackTrace(stream);
            stream.println();
        }
    }

}
