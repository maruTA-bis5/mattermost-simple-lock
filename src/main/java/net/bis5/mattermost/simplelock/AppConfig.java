package net.bis5.mattermost.simplelock;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
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

	public String getToken() {
		return env.getProperty("TOKEN");
	}

	public boolean isSameToken(String receivedToken) {
		return getToken().equals(receivedToken);
	}

	public static void setupSystemProperties() {
		if (StringUtils.isEmpty(System.getProperty("spring.redis.host"))) {
			String redisHost = System.getenv("REDIS_HOST");
			if (StringUtils.isNotEmpty(redisHost)) {
			System.setProperty("spring.redis.host", redisHost);
			}
		}
		if (StringUtils.isEmpty(System.getProperty("spring.redis.port"))) {
			String redisPort = System.getenv("REDIS_PORT");
			if (StringUtils.isNotEmpty(redisPort)) {
			System.setProperty("spring.redis.host", redisPort);
			}
		}
	}
}
