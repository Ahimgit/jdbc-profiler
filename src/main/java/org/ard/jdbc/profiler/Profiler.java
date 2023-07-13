package org.ard.jdbc.profiler;

import org.ard.jdbc.profiler.driver.util.StatsUtil;
import org.ard.jdbc.profiler.logging.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import static org.ard.jdbc.profiler.driver.StatsInfo.STAT_COUNT;
import static org.ard.jdbc.profiler.driver.StatsInfo.STAT_ELAPSED;
import static org.ard.jdbc.profiler.driver.StatsInfo.STAT_GROUP_STATEMENT;
import static org.ard.jdbc.profiler.driver.StatsInfo.STAT_GROUP_TOTALS;
import static org.ard.jdbc.profiler.driver.util.DriverLoaderUtil.findAlreadyRegisteredDriverForClass;
import static org.ard.jdbc.profiler.driver.util.StatsUtil.getStatsKey;

public class Profiler {

    private Profiler() {
    }

    public static class StackItem {

        private final long timeStart;
        private final Map<String, Map<String, Long>> jdbcStatsStart;

        public StackItem(final long timeStart, final Map<String, Map<String, Long>> jdbcStatsStart) {
            this.timeStart = timeStart;
            this.jdbcStatsStart = jdbcStatsStart;
        }

        public long getTimeStart() {
            return timeStart;
        }

        public Map<String, Map<String, Long>> getJdbcStatsStart() {
            return jdbcStatsStart;
        }
    }

    private static final ThreadLocal<Stack<StackItem>> stackTL = ThreadLocal.withInitial(Stack::new);

    private static final ThreadLocal<Map<String, Map<String, Long>>> callStats = ThreadLocal.withInitial(LinkedHashMap::new);

    public static volatile org.ard.jdbc.profiler.driver.Driver driver;
    public static Logger logger;

    private static void init() {
        if (driver == null) {
            synchronized (Profiler.class) {
                if (driver == null) {
                    driver = findAlreadyRegisteredDriverForClass(org.ard.jdbc.profiler.driver.Driver.class);
                    logger = driver.getDriverConfiguration().getLoggerFactory().getLogger("jdbc-profiler");
                }
            }
        }
    }

    public static void push() {
        try {
            init();
            stackTL.get().push(new StackItem(System.currentTimeMillis(), getCurrentJdbcStats()));
        } catch (final Exception e) {
            logger.error("Error at push()", e);
        }
    }

    public static void pop(final String op, final String message) {
        try {
            final Stack<StackItem> stack = stackTL.get();
            final StackItem stackItem = stack.pop();
            if (stackItem != null) {
                final long elapsed = System.currentTimeMillis() - stackItem.getTimeStart();
                final StringBuilder sb = new StringBuilder(256);
                for (int i = 0; i < stack.size(); i++) {
                    sb.append(".."); // pad to show stack depth
                }
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append("time: ").append(elapsed);
                final Map<String, Map<String, Long>> jdbcStatsStart = stackItem.getJdbcStatsStart();
                final Map<String, Map<String, Long>> jdbcStatsCurrent = getCurrentJdbcStats();
                final long elapsedStart = getStatsKey(jdbcStatsStart, STAT_GROUP_TOTALS, STAT_ELAPSED);
                final long elapsedCurrent = getStatsKey(jdbcStatsCurrent, STAT_GROUP_TOTALS, STAT_ELAPSED);
                final long countStart = getStatsKey(jdbcStatsStart, STAT_GROUP_STATEMENT, STAT_COUNT);
                final long countCurrent = getStatsKey(jdbcStatsCurrent, STAT_GROUP_STATEMENT, STAT_COUNT);
                final long db = elapsedCurrent - elapsedStart;
                final long dbn = countCurrent - countStart;
                sb.append(", db time: ").append(db).append(", count: ").append(dbn);
                sb.append(", ").append(message);
                sb.append(", stack:").append(StatsUtil.stackToString());
                logger.log(op, sb.toString());
                if (op != null) {
                    StatsUtil.statsAddElapsedWithCountOne(callStats.get(), op, elapsed);
                    StatsUtil.statsAddElapsedAndCount(callStats.get(), op + "_statement_count", db, dbn);
                }
            }
        } catch (final Exception e) {
            logger.error("Error at pop()", e);
        }
    }

    public static void statsTotal(final String op, final String message) {
        try {
            logger.log(op, new StringBuilder(1048).append(message)
                    .append("\n====== Total thread jdbc stats ===================\n")
                    .append(StatsUtil.statsToString(getCurrentJdbcStats()))
                    .append("====== Total thread profiler call stats ==========\n")
                    .append(StatsUtil.statsToString(callStats.get()))
                    .append("==================================================\n").toString());
        } catch (final Exception e) {
            logger.error("Error at statsTotal()", e);
        }
    }

    private static Map<String, Map<String, Long>> getCurrentJdbcStats() {
        return StatsUtil.statsCopy(driver.getStats());
    }

}