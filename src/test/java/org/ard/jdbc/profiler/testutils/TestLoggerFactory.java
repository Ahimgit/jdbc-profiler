package org.ard.jdbc.profiler.testutils;

import org.ard.jdbc.profiler.logging.Logger;
import org.ard.jdbc.profiler.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;

public class TestLoggerFactory implements LoggerFactory {

    public static class TestLogger implements Logger {

        public static Map<String, List<String>> state = new HashMap<>();

        private final String name;

        public TestLogger(final String name) {
            this.name = name;
        }

        public static List<String> getState(final String name) {
            return state.get(name);
        }

        public static void reset() {
            state = new HashMap<>();
        }

        @Override
        public void log(final String category, final String message) {
            add(format("{0} {1}", category, message));
        }

        @Override
        public void logSQL(final String category, final long elapsed, final String sql) {
            add(format("{0} time: {1}, sql: {2}", category, elapsed, sql));
        }

        @Override
        public void error(final String message, final Throwable th) {
            add(format("{0} {1}", message, th.getMessage()));
        }

        private void add(final String val) {
            state.computeIfAbsent(name, k -> new ArrayList<>()).add(val);
        }

    }

    @Override
    public Logger createLogger(final String name) {
        return new TestLogger(name);
    }

}
