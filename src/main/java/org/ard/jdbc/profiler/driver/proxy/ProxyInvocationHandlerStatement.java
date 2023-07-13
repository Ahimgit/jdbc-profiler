package org.ard.jdbc.profiler.driver.proxy;

import org.ard.jdbc.profiler.driver.StatsInfo;
import org.ard.jdbc.profiler.driver.util.StatsUtil;
import org.ard.jdbc.profiler.logging.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.ard.jdbc.profiler.driver.StatsInfo.STAT_GROUP_STATEMENT;
import static org.ard.jdbc.profiler.driver.util.StringUtil.replaceParameters;

public class ProxyInvocationHandlerStatement extends ProxyInvocationHandlerAbstract {

    private final ProxyFactory proxyFactory;

    private String query;
    private final Map<Object, Object> params;

    public ProxyInvocationHandlerStatement(final Object target,
                                           final ProxyFactory proxyFactory,
                                           final StatsInfo statsInfo,
                                           final Logger logger) {
        super(target, statsInfo, logger);
        this.proxyFactory = proxyFactory;
        this.params = new LinkedHashMap<>();
    }

    public ProxyInvocationHandlerStatement(final Object target,
                                           final String query,
                                           final ProxyFactory proxyFactory,
                                           final StatsInfo statsInfo,
                                           final Logger logger) {
        this(target, proxyFactory, statsInfo, logger);
        this.query = query;
    }

    @Override
    public Object proxyReturn(Object result, final Object[] args) {
        if (result instanceof ResultSet) {
            result = proxyFactory.getResultSetProxy((ResultSet) result);
        }
        return result;
    }

    @Override
    public void doFinally(final String methodName, final Object[] args, final long elapsed) {
        handleSavePreparedStatementParams(methodName, args);
        handleLogExecuteStatement(methodName, args, elapsed);
    }

    private void handleSavePreparedStatementParams(final String methodName, final Object[] args) {
        if (target instanceof PreparedStatement &&
                (methodName.startsWith("set") || methodName.startsWith("registerOutParameter"))
                && args != null && args.length > 1 && args[0] != null) {
            if (methodName.equals("setNull") || args[1] == null) {
                params.put(args[0], null);
            } else {
                params.put(args[0], args[1]);
            }
        }
    }

    private void handleLogExecuteStatement(final String methodName, final Object[] args,
                                           final long elapsed) {
        if (methodName.startsWith("execute")) {
            if (query == null && args != null && args.length > 0 && args[0] instanceof String) {
                query = (String) args[0];
            }
            if (query != null && params.size() > 0) {
                query = replaceParameters(query, params);
            }
            logger.logSQL(STAT_GROUP_STATEMENT, elapsed, query);
            StatsUtil.statsAddElapsedWithCountOne(statsInfo.getStats(), STAT_GROUP_STATEMENT, elapsed);
        }
    }

}


