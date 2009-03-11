package org.axiom.service;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.Registry;
import org.apache.commons.collections.Transformer;
import static org.apache.commons.collections.CollectionUtils.collect;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FilenameUtils.concat;
import org.axiom.integration.Environment;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import static org.axiom.integration.Environment.touch;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import static java.util.Arrays.asList;

@RunWith(JDaveRunner.class)
public class LauncherSpec extends Specification<Launcher> {

    public class WhenConfiguringHostEnvironment extends ServiceSpecSupport {

        private Launcher launcher;
        private ControlChannelBootstrapper mockBootstrapper
            = mock(ControlChannelBootstrapper.class);
        private ControlChannel mockChannel = mock(ControlChannel.class);

        public Launcher create() {
            mockContext = mock(CamelContext.class);
            mockRegistry = mock(Registry.class);
            mockConfig = mock(Configuration.class);

            return launcher = new Launcher(mockBootstrapper);
        }

        public void itShouldBootstrapAndThenStartTheService() throws ClassNotFoundException, IOException {
            final String conf = prepConfDirectory();
            one(mockBootstrapper).bootstrap(mockChannel);
            one(mockChannel).activate();
            stubContext();

            stubFileSystemEnvironment(conf);
            justIgnore(mockChannel, mockConfig);
            checking(this);

            launcher.launch(mockChannel);
        }

        public void itShouldLoadAllRouteScriptsPresentInTheConfDirectory() throws ClassNotFoundException, IOException {
            final String conf = prepConfDirectory();
            Collection routeScripts = collect(asList(1,2,3), new Transformer() {
                @Override public Object transform(final Object input) {
                    return touch(new File(concat(conf, "route" + input + ".rb")));
                }
            });

            allowing(mockBootstrapper).bootstrap((ControlChannel)with(anything()));
            allowing(mockChannel).activate();
            allowing(mockChannel).getRouteScriptEvaluator();
            will(returnValue(dummy(RouteConfigurationScriptEvaluator.class)));
            stubContext();
            stubFileSystemEnvironment(conf);

            for (final Object path : routeScripts) {
                one(mockChannel).configure(with(
                    routeLoaderFromScriptPath(((File)path).getAbsolutePath())));
            }

            checking(this);

            launcher.launch(mockChannel);
        }

        private void stubContext() {
            allowing(mockChannel).getContext();
            will(returnValue(mockContext));
        }

        private void stubFileSystemEnvironment(final String conf) throws ClassNotFoundException {
            stubRegistry();
            stubLookup(Environment.CONFIG_BEAN_ID, mockConfig);
            stubConfig(Environment.AXIOM_HOME,
                concat(Environment.TMPDIR, ".axiom"));
            stubConfig(Environment.SCRIPT_REPOSITORY_URI, conf);
            stubConfig(Environment.ENDORSED_PLUGINS,
                concat(concat(Environment.AXIOM_HOME, ".axiom"), "plugins"));
        }

        private String prepConfDirectory() throws IOException {
            final String conf = concat(concat(Environment.TMPDIR, ".axiom"), "conf");
            final File directory = new File(conf);
            if (directory.exists()) {
                FileUtils.deleteDirectory(directory);
            }
            FileUtils.forceMkdir(directory);
            return conf;
        }

    }

}
