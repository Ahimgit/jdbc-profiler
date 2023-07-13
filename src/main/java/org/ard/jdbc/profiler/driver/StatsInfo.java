package org.ard.jdbc.profiler.driver;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatsInfo {

    public static final String STAT_GROUP_TOTALS = "totals";
    public static final String STAT_GROUP_STATEMENT = "statement";
    public static final String STAT_ELAPSED = "elapsed";
    public static final String STAT_COUNT = "count";

    private final ThreadLocal<Map<String, Map<String, Long>>> stats;

    public StatsInfo() {
        stats = ThreadLocal.withInitial(LinkedHashMap::new);
    }

    public Map<String, Map<String, Long>> getStats() {
        return stats.get();
    }

}
