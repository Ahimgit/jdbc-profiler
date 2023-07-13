package org.ard.jdbc.profiler;

import org.ard.jdbc.profiler.testutils.TestLoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import static org.ard.jdbc.profiler.testutils.TestUtils.reRegisterDriver;

public class FunctionalTestBase {

    @BeforeAll
    public static void beforeAllBase() throws Exception {
        reRegisterDriver();
        Profiler.reset();
        TestLoggerFactory.TestLogger.reset();
    }

    @AfterEach
    public void afterEachBase() {
        TestLoggerFactory.TestLogger.reset();
        Profiler.reset();
    }

}
