package org.ard.jdbc.profiler.driver;

import org.ard.jdbc.profiler.driver.proxy.ProxyFactory;
import org.ard.jdbc.profiler.driver.util.StringUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Properties;

public class Driver implements java.sql.Driver {

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final StatsInfo statsInfo;
    private final ProxyFactory proxyFactory;
    private final DriverConfiguration driverConfiguration;

    public Driver() throws Exception {
        this.statsInfo = new StatsInfo();
        this.driverConfiguration = new DriverConfiguration();
        this.proxyFactory = new ProxyFactory(statsInfo, driverConfiguration);
    }

    public Map<String, Map<String, Long>> getStats() {
        return statsInfo.getStats();
    }

    public DriverConfiguration getDriverConfiguration() {
        return driverConfiguration;
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (acceptsURL(url)) {
            final java.sql.Driver realDriver = driverConfiguration.getRealDriver();
            final String realUrl = driverConfiguration.getRealURL();
            Connection conn = realDriver.connect(realUrl, info);
            if (conn != null) {
                conn = proxyFactory.getConnectionProxy(conn);
            }
            return conn;
        } else {
            // strange contract returning null indicates to driver manager to try another driver
            return null;
        }
    }

    @Override
    public boolean acceptsURL(final String url) {
        if (StringUtil.isProxyUrl(url)) {
            driverConfiguration.initialize(url);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties pr) throws SQLException {
        if (acceptsURL(url)) {
            final java.sql.Driver realDriver = driverConfiguration.getRealDriver();
            final String realUrl = driverConfiguration.getRealURL();
            return realDriver.getPropertyInfo(realUrl, pr);
        } else {
            return null;
        }
    }

    @Override
    public int getMajorVersion() {
        return driverConfiguration.isInitialized() ? driverConfiguration.getRealDriver().getMajorVersion() : 1;
    }

    @Override
    public int getMinorVersion() {
        return driverConfiguration.isInitialized() ? driverConfiguration.getRealDriver().getMinorVersion() : 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return !driverConfiguration.isInitialized() || driverConfiguration.getRealDriver().jdbcCompliant();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return driverConfiguration.isInitialized() ? driverConfiguration.getRealDriver().getParentLogger() : null;
    }

}
