package org.ard.jdbc.profiler.logging;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggerDefaultTest {

    @Test
    public void logTest() throws IOException {
        final Path file = Paths.get("test-logger.log");

        final Logger loggerDefault = new LoggerFactoryDefault().getLogger("test-logger");
        loggerDefault.log("cat", "msg");
        loggerDefault.logSQL("sqlCat", 321, "select 67 from dual");
        loggerDefault.error("msgErr", new RuntimeException("ew"));

        assertTrue(file.toFile().exists());
        file.toFile().deleteOnExit();

        final List<String> logLines = Files.readAllLines(file);
        final List<String> expected = new ArrayList<>();
        expected.add("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} .* \\[cat\\] msg");
        expected.add("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} .* \\[sqlCat\\] time: 321, sql: select 67 from dual");
        expected.add("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} .* \\[error\\] msgErr");
        expected.add("java.lang.RuntimeException: ew");
        expected.add("\\tat.*");
        assertLinesMatch(expected, logLines.subList(0, 5));
    }

}