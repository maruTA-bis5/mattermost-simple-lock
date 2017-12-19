package net.bis5.mattermost.simplelock;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfig {
	@Autowired
	Environment env;

	@Bean
	public EmbeddedServletContainerFactory createServletContainerFactory() {
		JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
		factory.setPort(Integer.valueOf(env.getProperty("PORT", "8888")));
		return factory;
	}

	public String getRedisKey(@NotNull String propertyName) {
		return env.getProperty("REDIS_PATH", "simple-lock") + ":" + propertyName;
	}

	public String getBaseUrl() {
		return env.getProperty("URL");
	}
}
