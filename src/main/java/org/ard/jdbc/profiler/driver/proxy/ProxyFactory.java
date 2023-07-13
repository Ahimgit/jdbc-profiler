package org.ard.jdbc.profiler.driver.proxy;

import org.ard.jdbc.profiler.driver.DriverConfiguration;
import org.ard.jdbc.profiler.driver.StatsInfo;
import org.ard.jdbc.profiler.logging.Logger;

import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProxyFactory {

    private final StatsInfo statsInfo;
    private final DriverConfiguration driverConfiguration;

    public ProxyFactory(final StatsInfo statsInfo, final DriverConfiguration driverConfiguration) {
        this.statsInfo = statsInfo;
        this.driverConfiguration = driverConfiguration;
    }

    public Connection getConnectionProxy(final Connection source) {
        return (Connection) Proxy.newProxyInstance(source.getClass().getClassLoader(),
                new Class[]{Connection.class},
                new ProxyInvocationHandlerConnection(source, this, statsInfo, getLogger()));
    }

    public CallableStatement getCallableStatementProxy(final CallableStatement source, final String sql) {
        return (CallableStatement) Proxy.newProxyInstance(source.getClass().getClassLoader(),
                new Class[]{CallableStatement.class},
                new ProxyInvocationHandlerStatement(source, sql, this, statsInfo, getLogger()));
    }

    public PreparedStatement getPreparedStatementProxy(final PreparedStatement source, final String sql) {
        return (PreparedStatement) Proxy.newProxyInstance(source.getClass().getClassLoader(),
                new Class[]{PreparedStatement.class},
                new ProxyInvocationHandlerStatement(source, sql, this, statsInfo, getLogger()));
    }

    public Statement getStatementProxy(final Statement source) {
        return (Statement) Proxy.newProxyInstance(source.getClass().getClassLoader(),
                new Class[]{Statement.class},
                new ProxyInvocationHandlerStatement(source, this, statsInfo, getLogger()));
    }

    public ResultSet getResultSetProxy(final ResultSet source) {
        return (ResultSet) Proxy.newProxyInstance(source.getClass().getClassLoader(),
                new Class[]{ResultSet.class},
                new ProxyInvocationHandlerSimple(source, statsInfo, getLogger()));
    }

    public DatabaseMetaData getDatabaseMetaDataProxy(final DatabaseMetaData source) {
        return (DatabaseMetaData) Proxy.newProxyInstance(source.getClass().getClassLoader(),
                new Class[]{DatabaseMetaData.class},
                new ProxyInvocationHandlerSimple(source, statsInfo, getLogger()));
    }

    private Logger getLogger() {
        return driverConfiguration.getLoggerFactory().getLogger("jdbc-profiler-sql");
    }

}
