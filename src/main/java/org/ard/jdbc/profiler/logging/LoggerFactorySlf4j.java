package org.ard.jdbc.profiler.logging;

public class LoggerFactorySlf4j implements LoggerFactory {

    @Override
    public Logger createLogger(final String name) {
        return new LoggerSlf4j(org.slf4j.LoggerFactory.getLogger(name));
    }

}

