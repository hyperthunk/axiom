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

import org.apache.commons.configuration.*;
import static org.apache.commons.lang.StringUtils.split;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.io.File.pathSeparator;

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
        LoggerFactory.getLogger(ExternalConfigurationSourceFactory.class);

    private final String defaultConfigurationSourcePath;

    /**
     * The system/default property which, if set, is used to locate
     * and load additional external properties when creating a new instance.
     */
    protected static final String AXIOM_CONFIGURATION_EXTERNALS =
        "axiom.configuration.externals";

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
     * Creates a new {@link org.apache.commons.configuration.Configuration} instance.
     * @return
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
