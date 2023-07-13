package org.ard.jdbc.profiler.testutils;

import org.ard.jdbc.profiler.Profiler;

public class TestUtils {

    public interface Block {
        void run() throws Exception;
    }

    private TestUtils() {
    }

    public static void profile(final String operation, final String message, final Block op) throws Exception {
        Profiler.push();
        try {
            op.run();
        } finally {
            Profiler.pop(operation, message);
        }
    }

    public static synchronized void withSystemProperty(final String property, final String value,
                                                       final Block code) throws Exception {
        final String prev = System.getProperty(property);
        try {
            System.setProperty(property, value);
            code.run();
        } finally {
            if (prev != null) {
                System.setProperty(property, prev);
            } else {
                System.clearProperty(property);
            }
        }
    }

    public static void runInThread(final Block code) throws Throwable {
        final Throwable[] th = new Throwable[1];
        final Thread t = new Thread(() -> {
            try {
                code.run();
            } catch (final Throwable the) {
                th[0] = the;
            }
        });
        t.start();
        t.join();
        if (th[0] != null) {
            throw th[0];
        }
    }

}
