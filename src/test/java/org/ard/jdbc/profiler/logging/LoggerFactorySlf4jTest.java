package org.ard.jdbc.profiler.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggerFactorySlf4jTest {

    @Test
    public void createLoggerTest() {
        final LoggerFactorySlf4j factoryDefault = new LoggerFactorySlf4j();
        final Logger logger = factoryDefault.createLogger("sl4j0");
        assertNotNull(logger);
        assertInstanceOf(LoggerSlf4j.class, logger);
    }

    @Test
    public void getLoggerSingletonTest() {
        final LoggerFactorySlf4j factory = new LoggerFactorySlf4j();
        final Logger logger1 = factory.getLogger("sl4j1");
        final Logger logger2 = factory.getLogger("sl4j2");
        final Logger logger11 = factory.getLogger("sl4j1");
        final Logger logger22 = factory.getLogger("sl4j2");
        assertNotNull(logger1);
        assertNotNull(logger2);
        assertNotNull(logger11);
        assertNotNull(logger22);
        assertNotEquals(logger1, logger2);
        assertEquals(logger1, logger11);
        assertEquals(logger2, logger22);
    }

}