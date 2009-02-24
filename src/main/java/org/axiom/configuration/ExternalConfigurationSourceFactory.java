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
 *
 *
 */

package org.axiom.configuration;

import org.apache.commons.configuration.*;
import static org.apache.commons.lang.StringUtils.split;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static java.io.File.pathSeparator;
import static java.lang.String.format;

/**
 * Factory for creating {@link org.apache.commons.configuration.Configuration}
 * instances. Loads System and (configurable) default properties, and allows
 * the user to provide a list of external properties files
 *
 */
public class ExternalConfigurationSourceFactory {

    private final Log log = LogFactory.getLog(getClass());
    private final String defaultConfigurationSourcePath;
    private static final String AXIOM_CONFIGURATION_EXTERNALS =
        "axiom.configuration.externals";
    protected static final String DEFAULT_PROPERTIES_LOG_MSG =
        "Configuring default properties in %s";

    public ExternalConfigurationSourceFactory() {
        this("axiom.properties");
    }

    public ExternalConfigurationSourceFactory(String defaultConfigurationSourcePath) {
        this.defaultConfigurationSourcePath = defaultConfigurationSourcePath;
    }

    public Configuration createConfiguration() {
        try {
            CompositeConfiguration config = new CompositeConfiguration();
            log.info("Configuring system properties");
            config.addConfiguration(new SystemConfiguration());
            log.info(format(DEFAULT_PROPERTIES_LOG_MSG, defaultConfigurationSourcePath));
            config.addConfiguration(new PropertiesConfiguration(defaultConfigurationSourcePath));
            configureAdditionalExternalProperties(config);
            return config;
        } catch (ConfigurationException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    private void configureAdditionalExternalProperties(CompositeConfiguration config) throws ConfigurationException {
        final String paths = config.getString(AXIOM_CONFIGURATION_EXTERNALS);
        log.info("Configuring additional (external) configuration elements");
        if (paths != null) {
            for (final String path :
                    split(paths, pathSeparator)) {
                log.info(String.format("Configuring external properties in %s", path));
                //TODO: support for other properties formats (xml, for instance)
                config.addConfiguration(new PropertiesConfiguration(path));
            }
        }
    }
}
