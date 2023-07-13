package org.ard.jdbc.profiler.driver.proxy;

import org.ard.jdbc.profiler.driver.StatsInfo;
import org.ard.jdbc.profiler.logging.Logger;

public class ProxyInvocationHandlerSimple extends ProxyInvocationHandlerAbstract {

    public ProxyInvocationHandlerSimple(final Object target, final StatsInfo statsInfo, final Logger logger) {
        super(target, statsInfo, logger);
    }

    @Override
    public Object proxyReturn(final Object result, final Object[] args) {
        return result; // return unwrapped result
    }

    @Override
    public void doFinally(final String methodName, final Object[] args, final long elapsed) {
        // nothing
    }

}
