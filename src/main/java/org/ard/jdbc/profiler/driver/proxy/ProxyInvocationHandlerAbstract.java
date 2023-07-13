package org.ard.jdbc.profiler.driver.proxy;

import org.ard.jdbc.profiler.driver.StatsInfo;
import org.ard.jdbc.profiler.driver.util.StatsUtil;
import org.ard.jdbc.profiler.logging.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ProxyInvocationHandlerAbstract implements InvocationHandler {

    protected final Object target;
    protected final StatsInfo statsInfo;
    protected final Logger logger;

    public ProxyInvocationHandlerAbstract(final Object target,
                                          final StatsInfo statsInfo,
                                          final Logger logger) {
        this.target = target;
        this.statsInfo = statsInfo;
        this.logger = logger;
    }

    abstract Object proxyReturn(Object result, Object[] args);

    abstract void doFinally(String methodName, Object[] args, long elapsed);

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final long startTime = System.currentTimeMillis();
        try {
            final Object result = method.invoke(target, args);
            return proxyReturn(result, args);
        } catch (final InvocationTargetException e) {
            logger.error("error in jdbc call", e);
            throw e.getCause() == null ? e : e.getCause();
        } catch (final Exception e) {
            logger.error("unexpected error in jdbc call", e);
            throw e;
        } finally {
            final String methodName = method.getName();
            if (!methodName.equals("equals")) {
                final long elapsed = System.currentTimeMillis() - startTime;
                doFinally(methodName, args, elapsed);
                recordMethodStats(method, elapsed);
            }
        }
    }

    private void recordMethodStats(final Method method, final long elapsed) {
        StatsUtil.statsAddElapsedWithCountOne(statsInfo.getStats(), StatsInfo.STAT_GROUP_TOTALS, elapsed);
        StatsUtil.statsAddMethodElapsed(statsInfo.getStats(), target.getClass(), method, elapsed);
    }

}
