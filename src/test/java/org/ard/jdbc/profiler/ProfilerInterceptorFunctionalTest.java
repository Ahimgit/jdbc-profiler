package org.ard.jdbc.profiler;

import org.ard.jdbc.profiler.testutils.TestLoggerFactory.TestLogger;
import org.ard.jdbc.profiler.testutils.TestSpringBootApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

@SpringBootTest(classes = TestSpringBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.main.banner-mode=off", "spring.main.log-startup-info=off"})
public class ProfilerInterceptorFunctionalTest {

    @BeforeAll
    public static void before() {
        TestLogger.reset();
        System.setProperty("jdbc_proxy_logger", "org.ard.jdbc.profiler.testutils.TestLoggerFactory");
    }

    @Test
    public void contextLoads() {
        final List<String> profLog = new ArrayList<>();
        profLog.add("invoke time: \\d+, db time: \\d+, count: 2, execution\\(DummyDao\\.createTable\\(\\)\\), stack:.*");
        profLog.add("invoke time: \\d+, db time: \\d+, count: 3, execution\\(DummyDao\\.insertData\\(\\)\\), stack:.*");
        profLog.add("invoke time: \\d+, db time: \\d+, count: 1, execution\\(DummyDao\\.selectWithPreparedStatement\\(..\\)\\), stack:.*");
        profLog.add("invoke .. time: \\d+, db time: \\d+, count: 1, execution\\(DummyDao\\.selectWithPreparedStatement\\(..\\)\\), stack:.*");
        profLog.add("invoke .. time: \\d+, db time: \\d+, count: 1, execution\\(AnotherDao\\.anotherSelectWithPreparedStatement\\(\\)\\), stack:.*");
        profLog.add("invoke time: \\d+, db time: \\d+, count: 2, execution\\(SomeService\\.someMethodToShowNesting\\(\\)\\), stack:.*");
        assertLinesMatch(profLog, TestLogger.getState("jdbc-profiler"));

        final List<String> sqlLog = new ArrayList<>();
        sqlLog.add("statement time: \\d+, sql: drop table if exists test_data");
        sqlLog.add("statement time: \\d+, sql: create table test_data\\(id int primary key, val varchar\\(255\\)\\)");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(1, 'testvalue1'\\)");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(3, 'testvalue2'\\)");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(4, 'testvalue3'\\)");
        sqlLog.add("statement time: \\d+, sql: select val from test_data where id between \\[\\?=1] and \\[\\?=4]");
        sqlLog.add("statement time: \\d+, sql: select val from test_data where id between \\[\\?=1] and \\[\\?=3]");
        sqlLog.add("statement time: \\d+, sql: select val from test_data where id = \\[\\?=365\\]");
        assertLinesMatch(sqlLog, TestLogger.getState("jdbc-profiler-sql"));
    }

    @AfterAll
    public static void after() {
        System.clearProperty("jdbc_proxy_logger");
    }

}
