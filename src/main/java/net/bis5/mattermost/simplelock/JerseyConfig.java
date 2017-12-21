package net.bis5.mattermost.simplelock;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import net.bis5.mattermost.jersey.provider.MattermostModelMapperProvider;

@Configuration
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		register(MattermostSimpleLockApplication.class);
		register(JacksonFeature.class);
		register(RequestContextFilter.class);
		register(new LoggingFeature(Logger.getAnonymousLogger(), Level.WARNING, Verbosity.PAYLOAD_TEXT, 2048));
		register(MattermostModelMapperProvider.class);
	}

}
