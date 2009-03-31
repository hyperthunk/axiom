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

package org.axiom;

import org.apache.camel.CamelContext;
import org.apache.commons.cli.*;
import static org.apache.commons.collections.CollectionUtils.*;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.Validate;
import org.axiom.integration.camel.ContextFactory;
import org.axiom.service.ControlChannel;
import org.axiom.service.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.*;
import java.security.Permission;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HostService {

    private static final Logger logger = LoggerFactory.getLogger(HostService.class);
    private static final Pattern regex = Pattern.compile("(?:-D)([^=]*)=(.*)");
    private static final String WAIT_TIME = "wait-timeout";
    private static final String TERMINATION_TIMEOUT = "termination-timeout";
    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();
    private static ControlChannel controlChannel;

    public static void main(final String... argv) throws ClassNotFoundException {
        final CommandLine cli = cli(argv);
        if (cli == null) {
            System.exit(1);
        }

        final SecurityManager originalSecurityManager = System.getSecurityManager();
        registerExitHandler();
        logger.info("Starting {}.",
            HostService.class.getCanonicalName());
        final CamelContext camel = new ContextFactory().create();

        try {
            controlChannel = new Launcher().launch(camel);
            if (cli.hasOption(WAIT_TIME)) {
                final String opt = cli.getOptionValue(WAIT_TIME);
                final Long timeout = Long.parseLong(opt);
                logger.info("{} idle (timeout={}seconds).",
                    HostService.class.getCanonicalName(), timeout);
                controlChannel.waitShutdown(timeout * 1000);
            } else {
                logger.info("{} idle (timeout=infinite).",
                    HostService.class.getCanonicalName());
                controlChannel.waitShutdown();
            }
        } catch (ExitStatusInterceptedException e) {
            if (e.getExitStatus() != 0 && cli.hasOption(TERMINATION_TIMEOUT)) {
                final Long timeout = Long.getLong(cli.getOptionValue(TERMINATION_TIMEOUT));
                controlChannel.sendShutdownSignalAndWait(timeout * 1000);
            }
            System.setSecurityManager(originalSecurityManager);
            System.exit(e.getExitStatus());
        }
    }

    private static void registerExitHandler() {
        System.setSecurityManager(
            new SecurityManager() {
                @Override public void checkPermission(Permission perm) {}
                @Override public void checkPermission(Permission perm, Object context) {}
                @Override public void checkExit(final int status) {
                    logger.info("Intercepting exit with status " + status);
                    throw new ExitStatusInterceptedException(status,
                        "Exit status " + status + " intercepted.");
                }
            }
        );
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    private static CommandLine cli(final String[] argv) {
        final String[] validArguments = stripOffSystemProperties(argv);
        Options options = new Options();
        options.addOption(OptionBuilder
            .withLongOpt(WAIT_TIME)
            .withDescription("Run for n seconds before exiting.")
            .hasArg()
            .withArgName("SECONDS")
            .isRequired(false)
            .create()
        );
        options.addOption(OptionBuilder
            .withLongOpt(TERMINATION_TIMEOUT)
            .withDescription("When exiting, terminate the control channel and wait for n seconds.")
            .hasArg()
            .withArgName("SECONDS")
            .isRequired(false)
            .create()
        );
        CommandLineParser parser = new PosixParser();
        try {
            return parser.parse(options, validArguments);
        } catch (ParseException e) {
            System.out.println(e.getLocalizedMessage());
            HELP_FORMATTER.printHelp(HostService.class.getCanonicalName(), options);
        }
        return null;
    }

    private static String[] stripOffSystemProperties(final String[] argv) {
        final List<String> arguments = new LinkedList<String>();
        addAll(arguments, argv);
        final Collection systemArguments = processSystemArguments(arguments);
        final Collection<String> disjoint =
            org.axiom.util.CollectionUtils.typedCollection(
                disjunction(
                    arguments,
                    systemArguments
                ), String.class
            );
        final String[] params = new String[disjoint.size()];
        return disjoint.toArray(params);
    }

    private static Collection processSystemArguments(final List<String> arguments) {
        final Collection<String> systemArguments = org.axiom.util
            .CollectionUtils.typedCollection(select(arguments,
            new Predicate() {
                @Override public boolean evaluate(final Object object) {
                    return regex.matcher(String.valueOf(object)).matches();
                }
            }
        ), String.class);
        for (final String arg : systemArguments) {
            final Matcher matcher = regex.matcher(arg);
            Validate.isTrue(matcher.find(),
                format("Expected %s to match %s!", arg, regex.toString()));
            final String name = matcher.group(1);
            final String value = matcher.group(2);
            System.setProperty(name, value);
        }
        return systemArguments;
    }

    public static class ExitStatusInterceptedException extends RuntimeException {

        private Integer exitStatus;

        public ExitStatusInterceptedException(final Integer exitStatus, final String message) {
            super(message);
            this.exitStatus = exitStatus;
        }

        public Integer getExitStatus() {
            return exitStatus;
        }
    }
}
