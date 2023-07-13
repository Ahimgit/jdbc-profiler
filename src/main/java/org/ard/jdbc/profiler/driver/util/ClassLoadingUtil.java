package org.ard.jdbc.profiler.driver.util;

public class ClassLoadingUtil {

    private ClassLoadingUtil() {
    }

    public static <T> T createInstance(final Class<T> type, final String className) throws Exception {
        final Class<?> clazz = loadClass(className);
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(className + " is not a class of type " + type);
        }
        return type.cast(clazz.getDeclaredConstructor().newInstance());
    }

    private static Class<?> loadClass(final String className) throws ClassNotFoundException {
        Class<?> loadedClass;
        try {
            // within container (e.g. Tomcat/JBoss) datasource can be provided by the container
            // and therefore only available in CCL
            loadedClass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (final ClassNotFoundException | SecurityException ignored) {
            loadedClass = Class.forName(className);
        }
        return loadedClass;
    }

}
