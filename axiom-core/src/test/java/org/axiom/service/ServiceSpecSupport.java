package org.axiom.service;

import org.apache.camel.CamelContext;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.Registry;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.axiom.SpecSupport;
import org.hamcrest.*;
import org.jmock.Mockery;

import static java.text.MessageFormat.*;

public class ServiceSpecSupport extends SpecSupport {

    protected CamelContext mockContext;
    protected Registry mockRegistry;
    protected Configuration mockConfig;
    protected Tracer mockTracer;

    public static Matcher<RouteScriptLoader> routeLoaderFromScriptPath(final String expectedScriptPath) {
        return new TypeSafeMatcher<RouteScriptLoader>() {
            @Override public boolean matchesSafely(final RouteScriptLoader routeScriptLoader) {
                return StringUtils.equals(expectedScriptPath, routeScriptLoader.getPathToScript());
            }

            @Override public void describeTo(final Description description) {
                description.appendText(format(
                    "Route Loader for Script at {0}.", expectedScriptPath));
            }
        };
    }

    protected void stubConfig(final String key, final String returns) {
        allowing(mockConfig).getString(key);
        will(returnValue(returns));
    }

    protected void stubConfig(final String key, final boolean returns) {
        allowing(mockConfig).getBoolean(key);
        will(returnValue(returns));
    }

    protected <T> void stubLookup(final String key, T value) throws ClassNotFoundException {
        Class<?> clazz = value.getClass();
        final int enhancerTagIdx = clazz.getName().indexOf("$$EnhancerByCGLIB$$");
        if (enhancerTagIdx > 0) {
            clazz = Class.forName(clazz.getName().substring(0, enhancerTagIdx));
        }
        allowing(mockRegistry).lookup(key, clazz);
        will(returnValue(value));
    }

    protected void stubRegistry() {
        allowing(mockContext).getRegistry();
        will(returnValue(mockRegistry));
    }

    protected void stubReadyToRunContext(final Mockery mockery) throws Exception {
        mockery.checking(new SpecSupport() {{
            stubConfiguration(mockContext, mockRegistry, mockConfig);
            allowing(mockRegistry).lookup(with(any(String.class)), with(any(Class.class)));
            will(returnValue(mockConfig));
            justIgnore(mockRegistry, mockConfig, mockTracer);

            allowing(mockContext).addInterceptStrategy((InterceptStrategy) with(anything()));
        }});
    }

    protected void prepareMocks(final Mockery mockery) {
        mockContext = mock(mockery, CamelContext.class);
        mockConfig = mock(mockery, Configuration.class);
        mockRegistry = mock(mockery, Registry.class);
        mockTracer = mock(mockery, Tracer.class);
    }

    protected <T> T mock(final Mockery mockery, final Class<T> classToMock) {
        return mockery.mock(classToMock, format("Mock[{0}]@[{1}]",
            classToMock.getCanonicalName(), getClass().getName()));
    }
}
