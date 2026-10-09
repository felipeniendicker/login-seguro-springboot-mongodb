package com.atividade.login.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.CookieSerializer.CookieValue;

class SessionConfigTest {

	@Test
	void deveConfigurarPersistenciaMongoNaColecaoDeSessoes() {
		EnableMongoHttpSession configuration =
				SessionConfig.class.getAnnotation(EnableMongoHttpSession.class);

		assertEquals("sessoes", configuration.collectionName());
		assertEquals(1800, configuration.maxInactiveIntervalInSeconds());
	}

	@Test
	void deveConfigurarCookieHttpOnlySameSiteLax() {
		String setCookie = escreverCookie(false);

		assertTrue(setCookie.startsWith("SESSION="));
		assertTrue(setCookie.contains("HttpOnly"));
		assertTrue(setCookie.contains("SameSite=Lax"));
		assertFalse(setCookie.contains("Secure"));
	}

	@Test
	void devePermitirCookieSecureEmProducao() {
		assertTrue(escreverCookie(true).contains("Secure"));
	}

	private String escreverCookie(boolean secure) {
		CookieSerializer serializer = new SessionConfig().cookieSerializer(secure);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		serializer.writeCookieValue(new CookieValue(
				request,
				response,
				"session-id-teste"));

		return response.getHeader("Set-Cookie");
	}
}
