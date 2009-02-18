package org.axiom.management;

import org.axiom.management.RouteConfigurationScriptEvaluator;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.core.io.ClassPathResource;
import static org.hamcrest.CoreMatchers.instanceOf;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.commons.io.FileUtils.readFileToString;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/server-context.xml"})
public class TestRouteConfigurationScriptEvaluatorSpringIntegration implements ApplicationContextAware {

    ApplicationContext applicationContext;
    @Autowired RouteConfigurationScriptEvaluator scriptEvaluator;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }    

    @Test
    public void injectedJRubyBeanShouldGenerateRouteBuilderFromScript() throws IOException {
        final ClassPathResource resource = new ClassPathResource("test-boot.rb");
        assertTrue("unable to find classpath test resource", resource.exists());
        final String script = readFileToString(resource.getFile());
        assertThat(scriptEvaluator.configure(script), instanceOf(RouteBuilder.class));
    }

}
