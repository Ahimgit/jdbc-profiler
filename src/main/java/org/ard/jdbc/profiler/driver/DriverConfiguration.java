package org.ard.jdbc.profiler.driver;

import org.ard.jdbc.profiler.logging.LoggerFactory;
import org.ard.jdbc.profiler.logging.LoggerFactoryDefault;

import java.sql.Driver;
import java.util.Map;

import static org.ard.jdbc.profiler.driver.util.ClassLoadingUtil.createInstance;
import static org.ard.jdbc.profiler.driver.util.DriverLoaderUtil.findAlreadyRegisteredDriver;
import static org.ard.jdbc.profiler.driver.util.DriverLoaderUtil.registerDriverByClassName;
import static org.ard.jdbc.profiler.driver.util.StringUtil.parseUrl;

public class DriverConfiguration {

    private final LoggerFactory loggerFactory;
    
    private volatile boolean initialized;
    private Driver realDriver;
    private String realURL;

    public DriverConfiguration() throws Exception {
        String loggerFactory = System.getProperty("jdbc_proxy_logger");
        loggerFactory = loggerFactory == null ? System.getenv("jdbc_proxy_logger") : loggerFactory;
        this.loggerFactory = loggerFactory == null
                ? new LoggerFactoryDefault()
                : createInstance(LoggerFactory.class, loggerFactory);
    }

    public Boolean isInitialized() {
        return initialized;
    }

    public Driver getRealDriver() {
        if (!isInitialized()) {
            throw new IllegalStateException("DriverInfo is not initialized!");
        }
        return realDriver;
    }

    public String getRealURL() {
        return realURL;
    }

    public LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public void initialize(final String url) {
        if (!this.initialized) {
            synchronized (this) {
                if (!this.initialized) {
                    try {
                        final Map<String, String> properties = parseUrl(url);
                        this.realURL = properties.get("url");
                        this.realDriver = properties.containsKey("class")
                                ? registerDriverByClassName(properties.get("class"))
                                : findAlreadyRegisteredDriver(this.realURL);
                        this.initialized = true;
                    } catch (final Exception e) {
                        throw new RuntimeException("Unable to init proxy driver for url " + url, e);
                    }
                }
            }
        }
    }

}
