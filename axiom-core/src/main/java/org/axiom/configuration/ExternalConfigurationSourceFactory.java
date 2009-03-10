/*
 * Copyright (c) 2009, Tim Watson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.axiom.configuration;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.Registry;
import org.apache.commons.configuration.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.*;
import org.axiom.service.LifecycleException;
import org.axiom.integration.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.io.File.*;

/**
 * Factory for creating {@link org.apache.commons.configuration.Configuration}
 * instances. Consumers can specify additional, external configurations by
 * setting the 'axiom.configuration.externals' property either as a system
 * property or in the default configuration (as passed to the constructor).
 *
 * This should contain either a single path or a list delimiter using the
 * operating system specific path separator char.
 */
public class ExternalConfigurationSourceFactory {

    private final Logger log =
        LoggerFactory.getLogger(getClass());

    private final String defaultConfigurationSourcePath;

    /**
     * The system/default property which, if set, is used to locate
     * and load additional external properties when creating a new instance.
     */
    protected static final String AXIOM_CONFIGURATION_EXTERNALS = "axiom.configuration.externals";

    /**
     * Initializes this with the default axiom properties.
     */
    public ExternalConfigurationSourceFactory() {
        this("axiom.properties");
    }

    /**
     * Initializes this with the default properties located in
     * the file at defaultConfigurationSourcePath
     * @param defaultConfigurationSourcePath The path to the default properties
     */
    public ExternalConfigurationSourceFactory(String defaultConfigurationSourcePath) {
        this.defaultConfigurationSourcePath = defaultConfigurationSourcePath;
    }

    /**
     * Get the registered {@link org.apache.commons.configuration.Configuration}
     * instance from the supplied {@link org.apache.camel.CamelContext}.
     * @param context The {@link org.apache.camel.CamelContext} in which the
     * {@link org.apache.commons.configuration.Configuration} instance is registered.
     * @return A registered {@link org.apache.commons.configuration.Configuration} instance
     * or {@code null } if no registered instance is found.
     */
    public static Configuration getRegisteredConfiguration(final CamelContext context) {
        notNull(context, "Camel context cannot be null.");
        return context.getRegistry().lookup(Environment.CONFIG_BEAN_ID, Configuration.class);
    }

    /**
     * Get the registered {@link org.apache.commons.configuration.Configuration}
     * instance from the supplied {@link Registry}.
     * @param registry The {@link Registry} in which the
     * {@link org.apache.commons.configuration.Configuration} instance is registered.
     * @return A registered {@link org.apache.commons.configuration.Configuration} instance
     * or {@code null } if no registered instance is found.
     */
    public static Configuration getRegisteredConfiguration(final Registry registry) {
        notNull(registry, "Registry cannot be null.");
        return registry.lookup(Environment.CONFIG_BEAN_ID, Configuration.class);
    }

    /**
     * As {@link ExternalConfigurationSourceFactory#getRegisteredConfiguration},
     * but throws {@link LifecycleException} if the configuration instance is not
     * properly registered in the supplied context.
     * @param registry The {@link Registry} in which the
     * {@link org.apache.commons.configuration.Configuration} instance is registered.
     * @return A registered {@link org.apache.commons.configuration.Configuration} instance
     * or {@code null } if no registered instance is found.
     */    
    public static Configuration requireRegisteredConfiguration(final Registry registry) {
        Configuration config = getRegisteredConfiguration(registry);
        if (config == null) {
            throw new LifecycleException(java.text.MessageFormat.format(
                "Context Registry is incorrectly configured: bean for id {0} is not present.",
                Environment.CONFIG_BEAN_ID
            ));
        }
        return config;
    }

    /**
     * As {@link ExternalConfigurationSourceFactory#getRegisteredConfiguration},
     * but throws {@link LifecycleException} if the configuration instance is not
     * properly registered in the supplied context.
     * @param context The {@link org.apache.camel.CamelContext} in which the
     * {@link org.apache.commons.configuration.Configuration} instance is registered.
     * @return A registered {@link org.apache.commons.configuration.Configuration} instance
     * or {@code null } if no registered instance is found.
     */
    public static Configuration requireRegisteredConfiguration(final CamelContext context) {
        return requireRegisteredConfiguration(context.getRegistry());
    }

    /**
     * Creates a new {@link org.apache.commons.configuration.Configuration} instance.
     * The returned {@link org.apache.commons.configuration.Configuration} is composed
     * of system properties, external (user defined) properties as defined by the
     * list of path locations in the {@code axiom.configuration.externals } system property
     * and default properties as defined by axiom-core.
     * @return A composite configuration of system properties, user supplied properties and
     * axiom's own internal and default properties.
     */
    public Configuration createConfiguration() {
        try {
            CompositeConfiguration config = new CompositeConfiguration();
            log.info("Configuring system properties.");
            config.addConfiguration(new SystemConfiguration());
            configureAdditionalExternalProperties(config);

            log.info("Configuring default properties in {}.", defaultConfigurationSourcePath);
            config.addConfiguration(new PropertiesConfiguration(defaultConfigurationSourcePath));
            return config;
        } catch (ConfigurationException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    private void configureAdditionalExternalProperties(CompositeConfiguration config) throws ConfigurationException {
        final String paths = config.getString(AXIOM_CONFIGURATION_EXTERNALS);
        if (paths != null) {
            for (final String path :
                    split(paths, pathSeparator)) {
                log.info("Configuring external properties in {}.", path);
                config.addConfiguration(new PropertiesConfiguration(path));
            }
        }
    }
}
