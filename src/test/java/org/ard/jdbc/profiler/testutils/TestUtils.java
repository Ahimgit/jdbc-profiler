package org.ard.jdbc.profiler.testutils;

import org.ard.jdbc.profiler.Profiler;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

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

    public static synchronized void reRegisterDriver() throws Exception {
        withSystemProperty("jdbc_proxy_logger", "org.ard.jdbc.profiler.testutils.TestLoggerFactory", () -> {
            final Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                final Driver driver = drivers.nextElement();
                if (driver instanceof org.ard.jdbc.profiler.driver.Driver) {
                    DriverManager.deregisterDriver(driver);
                }
            }
            DriverManager.registerDriver(new org.ard.jdbc.profiler.driver.Driver());
        });
    }

}
