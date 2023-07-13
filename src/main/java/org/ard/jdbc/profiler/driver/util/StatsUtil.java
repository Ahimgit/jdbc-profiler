package org.ard.jdbc.profiler.driver.util;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.ard.jdbc.profiler.driver.StatsInfo.STAT_COUNT;
import static org.ard.jdbc.profiler.driver.StatsInfo.STAT_ELAPSED;

public class StatsUtil {

    public static String stackToString() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final StringBuilder sb = new StringBuilder(1024);
        for (int i = 3; i < stackTrace.length; i++) {
            final StackTraceElement e = stackTrace[i];
            String name = e.getClassName();
            final int idx = name.lastIndexOf('.');
            if (idx > -1) {
                name = name.substring(idx + 1);
            }
            sb.append(name).append(".").append(e.getMethodName()).append(":").append(e.getLineNumber()).append(">");
        }
        return sb.toString();
    }

    public static Map<String, Map<String, Long>> statsCopy(final Map<String, Map<String, Long>> original) {
        final Map<String, Map<String, Long>> result = new LinkedHashMap<>();
        if (original != null) {
            for (final Map.Entry<String, Map<String, Long>> originalEntry : original.entrySet()) {
                final Map<String, Long> entry = new LinkedHashMap<>(originalEntry.getValue());
                result.put(originalEntry.getKey(), entry);
            }
        }
        return result;
    }

    public static void statsAddMethodElapsed(final Map<String, Map<String, Long>> statsMap,
                                             final Class<?> clazz, final Method method, final long elapsed) {
        final StringBuilder sb = new StringBuilder(256)
                .append(clazz.getSimpleName())
                .append(".")
                .append(method.getName());
        final Class<?>[] params = method.getParameterTypes();
        sb.append("(");
        for (int i = 0; i < params.length; i++) {
            final Class<?> c = params[i];
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(c.getSimpleName());
        }
        sb.append(")");
        statsAddElapsedAndCount(statsMap, sb.toString(), elapsed, 1);
    }

    public static void statsAddElapsedWithCountOne(final Map<String, Map<String, Long>> statsMap,
                                                   final String statGroup, final long elapsed) {
        statsAddElapsedAndCount(statsMap, statGroup, elapsed, 1);
    }

    public static void statsAddElapsedAndCount(final Map<String, Map<String, Long>> statsMap, final String statGroup,
                                               final long elapsed, final long count) {
        final Map<String, Long> statGroupMap = statsMap.computeIfAbsent(statGroup, k -> new LinkedHashMap<>());
        statsIncrementStat(statGroupMap, STAT_COUNT, count);
        statsIncrementStat(statGroupMap, STAT_ELAPSED, elapsed);
    }

    private static void statsIncrementStat(final Map<String, Long> statGroupMap, final String stat, final long incrementBy) {
        //if (incrementBy != 0) {
        Long statStart = statGroupMap.getOrDefault(stat, 0L);
        statStart += incrementBy;
        statGroupMap.put(stat, statStart);
        //}
    }

    public static long getStatsKey(final Map<String, Map<String, Long>> group, final String groupKey, final String key) {
        if (group == null) {
            return 0L;
        }
        final Map<String, Long> items = group.get(groupKey);
        return items == null ? 0L : items.getOrDefault(key, 0L);
    }

    public static String statsToString(final Map<String, Map<String, Long>> statsMap) {
        String result = null;
        if (statsMap != null) {
            final StringBuilder sb = new StringBuilder(1024);
            for (final Map.Entry<String, Map<String, Long>> kv : statsMap.entrySet()) {
                sb.append("- ").append(kv.getKey());
                for (final Map.Entry<String, Long> kvi : kv.getValue().entrySet()) {
                    sb.append(", ").append(kvi.getKey()).append(" = ").append(kvi.getValue());
                }
                sb.append("\n");
            }
            result = sb.toString();
        }
        return result;
    }

}
