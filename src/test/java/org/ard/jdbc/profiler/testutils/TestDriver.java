package org.ard.jdbc.profiler.testutils;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.util.Properties;
import java.util.logging.Logger;

public class TestDriver implements java.sql.Driver {

    @Override
    public Connection connect(final String url, final Properties info) {
        return null;
    }

    @Override
    public boolean acceptsURL(final String url) {
        return url.startsWith("jdbc:test");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }

}
