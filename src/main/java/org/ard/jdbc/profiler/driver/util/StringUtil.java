package org.ard.jdbc.profiler.driver.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StringUtil {

    public static final String URL_PREFIX = "proxy:";
    public static final String URL_END = "|";
    public static final String PARAM = "?";

    private StringUtil() {
    }

    public static boolean isProxyUrl(final String url) {
        return url != null && url.startsWith(URL_PREFIX);
    }

    public static Map<String, String> parseUrl(final String url) {
        final Map<String, String> result = new HashMap<>();
        final int idxPre = URL_PREFIX.length();
        final int idxEnd = url.indexOf(URL_END);
        if (idxEnd > -1) {
            result.put("url", url.substring(idxEnd + 1));
            final String[] properties = url.substring(idxPre, idxEnd).split(";");
            for (final String property : properties) {
                final String[] kv = property.split("=");
                result.put(kv[0], kv[1]);
            }
        } else {
            result.put("url", url.substring(idxPre));
        }
        return result;
    }

    public static String replaceParameters(final String query, final Map<Object, Object> params) {
        final StringBuilder result = new StringBuilder();
        int nextIdx;
        int lastIdx = 0;
        final Iterator<Object> paramIter = params.values().iterator();
        while ((nextIdx = query.indexOf(PARAM, lastIdx)) != -1 && paramIter.hasNext()) {
            final Object param = paramIter.next();
            result.append(query, lastIdx, nextIdx);
            result.append("[?=").append(param == null ? "null" : param.toString()).append("]");
            lastIdx = nextIdx + 1;
        }
        result.append(query, lastIdx, query.length());
        return result.toString();
    }

}
