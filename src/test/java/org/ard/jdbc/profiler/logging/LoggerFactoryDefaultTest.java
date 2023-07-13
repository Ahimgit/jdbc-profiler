package org.ard.jdbc.profiler.logging;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoggerFactoryDefaultTest {

    @Test
    public void createLoggerTest() {
        final LoggerFactoryDefault factoryDefault = new LoggerFactoryDefault();
        final Logger logger = factoryDefault.createLogger("t0");
        assertNotNull(logger);
        assertInstanceOf(LoggerDefault.class, logger);
    }

    @Test
    public void getLoggerSingletonTest() {
        final LoggerFactoryDefault factoryDefault = new LoggerFactoryDefault();
        final Logger logger1 = factoryDefault.getLogger("t1");
        final Logger logger2 = factoryDefault.getLogger("t2");
        final Logger logger11 = factoryDefault.getLogger("t1");
        final Logger logger22 = factoryDefault.getLogger("t2");
        assertNotNull(logger1);
        assertNotNull(logger2);
        assertNotNull(logger11);
        assertNotNull(logger22);
        assertNotEquals(logger1, logger2);
        assertEquals(logger1, logger11);
        assertEquals(logger2, logger22);
    }

    @AfterAll
    public static void afterAll() {
        new File("t0.log").deleteOnExit();
        new File("t1.log").deleteOnExit();
        new File("t2.log").deleteOnExit();
    }

}