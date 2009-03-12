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

package org.axiom.integration;

import org.apache.camel.CamelContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Environment support functions.
 */
public class Environment {

    // SYSTEM PROPERTIES

    /**
     * The value of the 'line separator' (OS independant).
     */
    public static final String NEWLINE = System.getProperty("line.separator");

    /**
     * The location of the 'tmp' directory (OS independant).
     */
    public static final String TMPDIR = System.getProperty("java.io.tmpdir");

    // APPLICATION PROPERTIES

    /**
     * The name of the property in which the uri (or path delimited
     * list of uris) for endorsed plugins resides.
     */
    public static final String ENDORSED_PLUGINS = "axiom.plugins.endorsed.uri";

    /**
     * The uri of the {@link CamelContext} in which axiom is being hosted,
     * which can be used to obtain an endpoint and/or exchange.
     */
    public static final String AXIOM_HOST_URI = "axiom:host";

    /**
     * The property key of the default procecessing node for the axiom control
     * channel. A lookup against this key will return the bean id, which can then
     * be used to resolve an instance at runtime.
     */
    public static final String DEFAULT_PROCESSOR = "axiom.processors.default.id";

    /**
     * The bean id of the registered composite configuration instance
     * for the host context (Spring, JNDI, etc). This is the key under which
     * the {@link Configuration} instance is registered, not a key into
     * any property/configuration store.
     */
    public static final String CONFIG_BEAN = "axiom.configuration";

    /**
     * The property key for the uri on which the (camel) control channel
     * resides within the host/managed context.
     */
    public static final String CONTROL_CHANNEL = "axiom.channels.control";

    /**
     * The property key for the uri on which the (camel) termination channel resides.
     * Messages to this channel are an indication that system shutdown has been
     * requested by a consumer. Application clients can use this to determine
     * when to terminate the process. 
     */
    public static final String TERMINATION_CHANNEL = "axiom.channels.shutdown";

    /**
     * The property key of the <b>HOME</b> directory for axiom.
     */
    public static final String AXIOM_HOME = "axiom.home";

    /**
     * The property key of the directory in which route scripts can be stored.
     */
    public static final String SCRIPT_REPOSITORY_URI = "axiom.scripts.repository.uri";

    /**
     * The property key mapping the array of file extensions which are valid (i.e. will
     * be searched for) in route script files. 
     */
    public static final String SCRIPT_FILE_EXTENSIONS = "axiom.scripts.file.extensions";

    /**
     * The property name used to identify the service id (JNDI uri or Spring Bean name)
     * for the default registered instance (or prototype) or this type.
     */
    public static final String ROUTE_SCRIPT_EVALUATOR = "axiom.processors.route.evaluator.id";

    /**
     * The property name used to identify the service id (JNDI uri or Spring Bean name)
     * for the default registered instance (or prototype) or this type.
     */
    public static final String CODE_EVALUATOR = "axiom.processors.code.eval.id";

    /**
     * The termination signal id.
     */
    public static final String SIG_TERMINATE = "terminate";
    public static final String SIGNAL = "signal";
    public static final String SIG_CONFIGURE = "configure";

    /**
     * Ensures that the file system is properly configured, based on the supplied
     * properties (e.g., checks that the configured {@code axiom.home} directory
     * exists, etc).
     * @param config The configuration settings to use.
     */
    public static void prepareFileSystem(final Configuration config) {
        final File axiomHome = new File(config.getString(AXIOM_HOME));
        final File routeScriptsDir = new File(config.getString(SCRIPT_REPOSITORY_URI));
        final String endorsedPlugins = config.getString(ENDORSED_PLUGINS);

        ensureDirectory(axiomHome);
        ensureDirectory(routeScriptsDir);
        for (final String path : endorsedPlugins.split(File.pathSeparator)) {
            if (StringUtils.isNotEmpty(path)) {
                ensureDirectory(new File(path));
            }
        }
    }

    /**
     * Ensures that a directory path exists on the file system, creating it
     * (an any intermediate paths ) if not already present. Pukes with a
     * {@link RuntimeException} in the face of any errors.
     * @param path The path to ensure.
     */
    public static void ensureDirectory(final File path) {
        if (!path.exists()) {
            try {
                forceMkdir(path);
            } catch (IOException e) {
                throw new RuntimeException(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Helper for {@code FileUtils.touch} which wraps any {@link IOException}
     * in a {@link RuntimeException}. 
     * @param file The input file to touch.
     * @return {@code file}
     */
    public static File touch(final File file) {
        try {
            FileUtils.touch(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }
}
