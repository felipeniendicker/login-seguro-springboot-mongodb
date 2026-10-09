package com.atividade.login.config;

import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration(proxyBeanMethods = false)
@EnableMongoHttpSession(
		collectionName = "sessoes",
		maxInactiveIntervalInSeconds = 1800)
public class SessionConfig {

	@Bean
	public CookieSerializer cookieSerializer(
			@Value("${server.servlet.session.cookie.secure:false}") boolean secure) {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName("SESSION");
		serializer.setUseHttpOnlyCookie(true);
		serializer.setSameSite("Lax");
		serializer.setUseSecureCookie(secure);
		return serializer;
	}
}
