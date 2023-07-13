package org.ard.jdbc.profiler.logging;

public interface Logger {

    void log(String category, String message);

    void logSQL(String category, long elapsed, String sql);

    void error(String message, Throwable th);

}
