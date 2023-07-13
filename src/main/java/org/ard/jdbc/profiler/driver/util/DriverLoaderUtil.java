package org.ard.jdbc.profiler.driver.util;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import static org.ard.jdbc.profiler.driver.util.ClassLoadingUtil.createInstance;

public class DriverLoaderUtil {

    private DriverLoaderUtil() {
    }

    public static Driver registerDriverByClassName(final String className) throws Exception {
        final Driver driver = createInstance(Driver.class, className);
        DriverManager.registerDriver(driver);
        return driver;
    }

    public static <T extends Driver> T findAlreadyRegisteredDriverForClass(final Class<T> driverClass) {
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            final Driver driver = drivers.nextElement();
            if (driverClass.isAssignableFrom(driver.getClass())) {
                return driverClass.cast(driver);
            }
        }
        return null;
    }

    public static Driver findAlreadyRegisteredDriver(final String realURL) throws SQLException {
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            final Driver driver = drivers.nextElement();
            if (driver.acceptsURL(realURL)) {
                return driver;
            }
        }
        throw new IllegalStateException("Proxied driver not found for url " + realURL);
    }

}
