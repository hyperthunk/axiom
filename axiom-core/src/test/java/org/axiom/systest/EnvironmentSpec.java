package org.axiom.systest;

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FilenameUtils.*;
import org.apache.commons.lang.StringUtils;
import org.axiom.SpecSupport;
import org.axiom.integration.Environment;
import org.hamcrest.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(JDaveRunner.class)
public class EnvironmentSpec extends Specification<Environment> {

    public class WhenInteractingWithTheFileSystem extends SpecSupport {

        public void itShouldWrapIOExceptionsWhilstOperatingOnFiles() {
            final File file = mock(File.class);
            one(file).exists(); will(returnValue(false));
            one(file).exists(); will(returnValue(true));
            allowing(file).isDirectory(); will(returnValue(false));
            allowing(file).canWrite(); will(returnValue(false));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    Environment.touch(file);
                }
            }, should.raise(RuntimeException.class));
        }

    }

    public class WhenEnsuringTheFileSystemIsProperlyConfigured {

        private final String endorsedPlugins =
            String.format("plugins%scustomer-plugins", File.pathSeparator);
        private Configuration config = new SystemConfiguration() {{
            setProperty(Environment.AXIOM_HOME,
                concat(Environment.TMPDIR, ".axiom"));
            setProperty(Environment.SCRIPT_REPOSITORY_URI,
                concat(concat(Environment.TMPDIR, ".axiom"), "conf"));

            String plugins = "";
            for (final String pluginDir : endorsedPlugins.split(File.pathSeparator)) {
                plugins +=
                    concat(concat(Environment.TMPDIR, ".axiom"), pluginDir) + File.pathSeparator;
            }
            setProperty(Environment.ENDORSED_PLUGINS, plugins);
        }};

        public void itShouldCreateTheAxiomHomeDiretoryOnDemand() throws IOException {
            checkFileSystemProperlyEnsured(Environment.AXIOM_HOME);
        }

        public void itShouldCreateTheRouteScriptsDirectoryOnDemand() throws IOException {
            checkFileSystemProperlyEnsured(Environment.SCRIPT_REPOSITORY_URI);
        }

        public void itShouldCreateTheEndorsedPluginDirectoriesOnDemand() throws IOException {
            final String[] paths = endorsedPlugins.split(File.pathSeparator);
            for (final String path : paths) {
                deleteIfExists(new File(path));
            }
            Environment.prepareFileSystem(config);
            
            for (final String path : paths) {
                if (StringUtils.isNotEmpty(path)) {
                    final String fullPath =
                        concat(config.getString(Environment.AXIOM_HOME), path);
                    specify(new File(fullPath), exists());
                }
            }
        }

        private void checkFileSystemProperlyEnsured(final String property) throws IOException {
            final File tmpDir = new File(config.getString(property));
            enforceFileSystemBehaviorChecks(tmpDir);
        }

        private void enforceFileSystemBehaviorChecks(final File tmpDir) throws IOException {
            deleteIfExists(tmpDir);
            Environment.prepareFileSystem(config);
            specify(tmpDir, exists());
        }

        private void deleteIfExists(final File tmpDir) throws IOException {
            if (tmpDir.exists()) {
                FileUtils.deleteDirectory(tmpDir);
            }
        }
    }

    public static Matcher<File> exists() {
        return new TypeSafeMatcher<File>() {
            @Override public boolean matchesSafely(final File file) {
                return file.exists();
            }

            @Override public void describeTo(final Description description) {
                description.appendText("File existence check.");
            }
        };
    }


}
