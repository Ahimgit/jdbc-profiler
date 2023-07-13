package org.ard.jdbc.profiler;

import org.ard.jdbc.profiler.testutils.TestLoggerFactory.TestLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.ard.jdbc.profiler.testutils.TestUtils.profile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProfilerStandaloneFunctionalTest extends FunctionalTestBase {

    private Connection conn;

    @Test
    public void testInsertAndSelectWithProfiler() throws Exception {
        profile("profiler_totalx", "all operations", () -> {
            profile("profiler_create", "create tbls", this::createTable);
            profile("profiler_insert", "insert data", this::insertData);
            profile("profiler_forxxx", "wrapp block", () -> {
                for (int i = 0; i < 3; i++) {
                    profile("profiler_select", "select with p statement", this::selectWithPreparedStatement);
                }
            });
        });

        final List<String> sqlLog = new ArrayList<>();
        sqlLog.add("statement time: \\d+, sql: drop table if exists test_data");
        sqlLog.add("statement time: \\d+, sql: drop table if exists test_data");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(1, 'testvalue'\\)");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(1, 'testvalue'\\)");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(1, 'testvalue'\\)");
        sqlLog.add("statement time: \\d+, sql: select val from test_data where id=\\[\\?=1\\] or val=\\[\\?=a'2'b\\]");
        sqlLog.add("statement time: \\d+, sql: select val from test_data where id=\\[\\?=1\\] or val=\\[\\?=a'2'b\\]");
        sqlLog.add("statement time: \\d+, sql: select val from test_data where id=\\[\\?=1\\] or val=\\[\\?=a'2'b\\]");
        assertLinesMatch(sqlLog, TestLogger.getState("jdbc-profiler-sql"));

        final List<String> profLog = new ArrayList<>();
        profLog.add("profiler_create .. time: \\d+, db time: \\d+, count: 2, create tbls, stack:.*");
        profLog.add("profiler_insert .. time: \\d+, db time: \\d+, count: 3, insert data, stack:.*");
        profLog.add("profiler_select .... time: \\d+, db time: \\d+, count: 1, select with p statement, stack:.*");
        profLog.add("profiler_select .... time: \\d+, db time: \\d+, count: 1, select with p statement, stack:.*");
        profLog.add("profiler_select .... time: \\d+, db time: \\d+, count: 1, select with p statement, stack:.*");
        profLog.add("profiler_forxxx .. time: \\d+, db time: \\d+, count: 3, wrapp block, stack:.*");
        profLog.add("profiler_totalx time: \\d+, db time: \\d+, count: 8, all operations, stack:.*");
        assertLinesMatch(profLog, TestLogger.getState("jdbc-profiler"));
    }

    @Test
    public void testSQLOnly() throws Exception {
        createTable();
        insertData();
        selectWithPreparedStatement();
        final List<String> sqlLog = new ArrayList<>();
        sqlLog.add("statement time: \\d+, sql: drop table if exists test_data");
        sqlLog.add("statement time: \\d+, sql: drop table if exists test_data");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(1, 'testvalue'\\)");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(1, 'testvalue'\\)");
        sqlLog.add("statement time: \\d+, sql: insert into test_data \\(id, val\\) values \\(1, 'testvalue'\\)");
        sqlLog.add("statement time: \\d+, sql: select val from test_data where id=\\[\\?=1\\] or val=\\[\\?=a'2'b\\]");
        assertLinesMatch(sqlLog, TestLogger.getState("jdbc-profiler-sql"));
        assertNull(TestLogger.getState("jdbc-profiler"));
    }


    @Test
    public void testTotalsWithProfiler() throws Throwable {
            profile("profiler_create", "create tbls", this::createTable);
            Profiler.statsTotal("profiler_everything", "prints everything");

            final List<String> sqlLog = new ArrayList<>();
            sqlLog.add("statement time: \\d+, sql: drop table if exists test_data");
            sqlLog.add("statement time: \\d+, sql: drop table if exists test_data");
            assertLinesMatch(sqlLog, TestLogger.getState("jdbc-profiler-sql"));

            final List<String> profLog = new ArrayList<>();
            profLog.add("profiler_create time: \\d+, db time: \\d+, count: 2, create tbls, stack:.*");
            profLog.add("profiler_everything prints everything\\n" +
                    "====== Total thread jdbc stats ===================\n" +
                    "- totals, count = 4, elapsed = \\d+\\n" +
                    "- JdbcConnection.createStatement\\(\\), count = 1, elapsed = \\d+\\n" +
                    "- statement, count = 2, elapsed = \\d+\\n" +
                    "- JdbcStatement.execute\\(String\\), count = 2, elapsed = \\d+\\n" +
                    "- JdbcStatement.close\\(\\), count = \\d+, elapsed = \\d+\\n" +
                    "====== Total thread profiler call stats ==========\n" +
                    "- profiler_create, count = 1, elapsed = \\d+\\n" +
                    "- profiler_create_statement_count, count = 2, elapsed = \\d+\\n" +
                    "==================================================\\n"
            );
            assertLinesMatch(profLog, TestLogger.getState("jdbc-profiler"));
    }


    @BeforeEach
    public void beforeEach() throws Exception {
        conn = DriverManager.getConnection("proxy:jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
    }

    @AfterEach
    public void afterEach() throws Exception {
        conn.close();
    }

    private void createTable() throws SQLException {
        try (final Statement st = conn.createStatement()) {
            st.execute("drop table if exists test_data");
            st.execute("create table test_data(id int primary key, val varchar(255))");
        }
    }

    private void insertData() throws SQLException {
        try (final Statement st = conn.createStatement()) {
            st.execute("insert into test_data (id, val) values (1, 'testvalue')");
            st.execute("insert into test_data (id, val) values (3, 'testvalue')");
            st.execute("insert into test_data (id, val) values (4, 'testvalue')");
        }
    }

    private void selectWithPreparedStatement() throws SQLException {
        try (final PreparedStatement st = conn.prepareStatement("select val from test_data where id=? or val=?")) {
            st.setInt(1, 1);
            st.setString(2, "a'2'b");
            try (final ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    assertEquals("testvalue", rs.getString("val"));
                }
            }
        }
    }

}
