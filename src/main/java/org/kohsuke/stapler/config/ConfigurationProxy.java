package org.kohsuke.stapler.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

/**
 * @author Kohsuke Kawaguchi
 */
public class ConfigurationProxy {
    public static <T> T create(final Properties config, Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass()==Object.class)
                    return method.invoke(this,args);
                
                Class<?> r = method.getReturnType();
                String key = getKey(method);
                if (r==String.class)
                    return config.getProperty(key);

                throw new IllegalStateException("Unexpected return type: "+r);
            }

            private String getKey(Method method) {
                // TODO: support annotation
                return method.getName();
            }
        }));
    }
}
