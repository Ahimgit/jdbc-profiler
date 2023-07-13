package org.ard.jdbc.profiler.driver;

import org.ard.jdbc.profiler.logging.LoggerFactoryDefault;
import org.ard.jdbc.profiler.testutils.TestDriver;
import org.ard.jdbc.profiler.testutils.TestLoggerFactory;
import org.junit.jupiter.api.Test;

import static org.ard.jdbc.profiler.testutils.TestUtils.withSystemProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DriverConfigurationTest {

    @Test
    public void testCustomLoggerFactory() throws Exception {
        withSystemProperty("jdbc_proxy_logger",
                "org.ard.jdbc.profiler.testutils.TestLoggerFactory", () ->
                        assertInstanceOf(TestLoggerFactory.class, new DriverConfiguration().getLoggerFactory()));
    }

    @Test
    public void testDefaultLoggerFactory() throws Exception {
        assertInstanceOf(LoggerFactoryDefault.class, new DriverConfiguration().getLoggerFactory());
    }

    @Test
    public void testServiceLoaderSupportedDriver() throws Exception {
        final DriverConfiguration driverConfiguration = new DriverConfiguration();
        assertFalse(driverConfiguration.isInitialized());
        driverConfiguration.initialize("proxy:jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        assertTrue(driverConfiguration.isInitialized());
        assertEquals("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driverConfiguration.getRealURL());
        assertInstanceOf(org.h2.Driver.class, driverConfiguration.getRealDriver());
    }

    @Test
    public void testForceLoadedDriver() throws Exception {
        final DriverConfiguration driverConfiguration = new DriverConfiguration();
        assertFalse(driverConfiguration.isInitialized());
        driverConfiguration.initialize("proxy:class=org.ard.jdbc.profiler.testutils.TestDriver|jdbc:test:whatever;p1=v1;p2=v2");
        assertTrue(driverConfiguration.isInitialized());
        assertEquals("jdbc:test:whatever;p1=v1;p2=v2", driverConfiguration.getRealURL());
        assertInstanceOf(TestDriver.class, driverConfiguration.getRealDriver());
    }

}