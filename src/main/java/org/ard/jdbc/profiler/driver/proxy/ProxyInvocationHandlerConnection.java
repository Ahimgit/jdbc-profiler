package org.ard.jdbc.profiler.driver.proxy;

import org.ard.jdbc.profiler.driver.StatsInfo;
import org.ard.jdbc.profiler.driver.util.StatsUtil;
import org.ard.jdbc.profiler.logging.Logger;

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class ProxyInvocationHandlerConnection extends ProxyInvocationHandlerAbstract {

    private final ProxyFactory proxyFactory;

    public ProxyInvocationHandlerConnection(final Object target,
                                            final ProxyFactory proxyFactory,
                                            final StatsInfo statsInfo,
                                            final Logger logger) {
        super(target, statsInfo, logger);
        this.proxyFactory = proxyFactory;
    }

    @Override
    public Object proxyReturn(Object result, final Object[] args) {
        if (result instanceof CallableStatement) {
            result = proxyFactory.getCallableStatementProxy((CallableStatement) result, args[0].toString());
        } else if (result instanceof PreparedStatement) {
            result = proxyFactory.getPreparedStatementProxy((PreparedStatement) result, args[0].toString());
        } else if (result instanceof Statement) {
            result = proxyFactory.getStatementProxy((Statement) result);
        } else if (result instanceof DatabaseMetaData) {
            result = proxyFactory.getDatabaseMetaDataProxy((DatabaseMetaData) result);
        }
        return result;
    }

    @Override
    public void doFinally(final String methodName, final Object[] args, final long elapsed) {
        if (methodName.equals("commit")) {
            logger.logSQL(StatsInfo.STAT_GROUP_STATEMENT, elapsed, "commit");
            StatsUtil.statsAddElapsedWithCountOne(statsInfo.getStats(), StatsInfo.STAT_GROUP_STATEMENT, elapsed);
        } else if (methodName.equals("rollback")) {
            logger.logSQL(StatsInfo.STAT_GROUP_STATEMENT, elapsed, "rollback");
            StatsUtil.statsAddElapsedWithCountOne(statsInfo.getStats(), StatsInfo.STAT_GROUP_STATEMENT, elapsed);
        }
    }
}
