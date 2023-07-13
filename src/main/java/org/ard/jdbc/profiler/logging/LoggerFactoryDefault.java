package org.ard.jdbc.profiler.logging;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class LoggerFactoryDefault implements LoggerFactory {

    @Override
    public Logger createLogger(final String name) {
        try {
            final PrintStream stream = new PrintStream(new FileOutputStream(name + ".log", false),
                    false, "UTF-8");
            Runtime.getRuntime().addShutdownHook(new Thread(stream::close));
            return new LoggerDefault(stream);
        } catch (final FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
