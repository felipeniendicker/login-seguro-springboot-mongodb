package com.atividade.login.controller;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.atividade.login.config.SecurityConfig;
import com.atividade.login.dto.CadastroUsuarioRequest;
import com.atividade.login.model.Usuario;
import com.atividade.login.repository.UsuarioRepository;
import com.atividade.login.service.UsuarioDetailsService;
import com.atividade.login.service.UsuarioService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(controllers = {AuthController.class, UsuarioController.class})
@Import({SecurityConfig.class, UsuarioDetailsService.class})
class AuthSessionControllerTest {

	private static final String EMAIL = "usuario@exemplo.test";
	private static final String SENHA = "senha-segura-123";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private UsuarioService usuarioService;

	@Test
	void deveAutenticarCriarSessaoEAcessarUsuarioAtual() throws Exception {
		Usuario usuario = criarUsuario();
		when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

		MvcResult loginResult = mockMvc.perform(postComCsrf("/api/auth/login")
					.contentType("application/json")
					.content(loginJson(SENHA)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("usuario-id-teste"))
				.andExpect(jsonPath("$.nome").value("Usuário Teste"))
				.andExpect(jsonPath("$.email").value(EMAIL))
				.andExpect(jsonPath("$.perfil").value("USUARIO"))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.token").doesNotExist())
				.andExpect(content().string(not(containsString(usuario.getSenha()))))
				.andReturn();

		MockHttpSession session =
				(MockHttpSession) loginResult.getRequest().getSession(false);
		assertNotNull(session);
		assertNotNull(session.getAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("usuario-id-teste"))
				.andExpect(jsonPath("$.email").value(EMAIL))
				.andExpect(jsonPath("$.perfil").value("USUARIO"))
				.andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	void deveRejeitarSenhaIncorretaSemCriarSessao() throws Exception {
		when(usuarioRepository.findByEmail(EMAIL))
				.thenReturn(Optional.of(criarUsuario()));

		MvcResult result = mockMvc.perform(postComCsrf("/api/auth/login")
					.contentType("application/json")
					.content(loginJson("senha-incorreta")))
				.andExpect(status().isUnauthorized())
				.andReturn();

		assertNull(result.getRequest().getSession(false));
	}

	@Test
	void deveRejeitarUsuarioInexistente() throws Exception {
		when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

		mockMvc.perform(postComCsrf("/api/auth/login")
					.contentType("application/json")
					.content(loginJson(SENHA)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveBloquearUsuarioAtualSemAutenticacao() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveInvalidarSessaoNoLogout() throws Exception {
		Usuario usuario = criarUsuario();
		when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

		MvcResult loginResult = mockMvc.perform(postComCsrf("/api/auth/login")
					.contentType("application/json")
					.content(loginJson(SENHA)))
				.andExpect(status().isOk())
				.andReturn();
		MockHttpSession session =
				(MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(postComCsrf("/api/auth/logout")
					.session(session))
				.andExpect(status().isNoContent());

		assertTrue(session.isInvalid());
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveManterCadastroPublicoComCsrf() throws Exception {
		when(usuarioService.cadastrar(any(CadastroUsuarioRequest.class)))
				.thenReturn(criarUsuario());

		mockMvc.perform(postComCsrf("/api/usuarios/cadastro")
					.contentType("application/json")
					.content("""
							{
							  "nome": "Usuário Teste",
							  "email": "usuario@exemplo.test",
							  "senha": "senha-segura-123"
							}
							"""))
				.andExpect(status().isCreated());
	}

	@Test
	void deveExigirCsrfNoLogin() throws Exception {
		mockMvc.perform(post("/api/auth/login")
					.contentType("application/json")
					.content(loginJson(SENHA)))
				.andExpect(status().isForbidden());
	}

	@Test
	void deveFornecerCsrfSemCriarSessao() throws Exception {
		MvcResult result = obterCsrf();

		assertNull(result.getRequest().getSession(false));
	}

	private MvcResult obterCsrf() throws Exception {
		return mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
				.andExpect(jsonPath("$.token", not(blankOrNullString())))
				.andExpect(cookie().exists("XSRF-TOKEN"))
				.andReturn();
	}

	private MockHttpServletRequestBuilder postComCsrf(String endpoint) throws Exception {
		Cookie csrfCookie = obterCsrf().getResponse().getCookie("XSRF-TOKEN");
		assertNotNull(csrfCookie);

		return post(endpoint)
				.cookie(csrfCookie)
				.header("X-XSRF-TOKEN", csrfCookie.getValue());
	}

	private Usuario criarUsuario() {
		Usuario usuario = new Usuario();
		ReflectionTestUtils.setField(usuario, "id", "usuario-id-teste");
		usuario.setNome("Usuário Teste");
		usuario.setEmail(EMAIL);
		usuario.setSenha(new BCryptPasswordEncoder().encode(SENHA));
		usuario.setPerfil("USUARIO");
		return usuario;
	}

	private String loginJson(String senha) {
		return """
				{
				  "email": "usuario@exemplo.test",
				  "senha": "%s"
				}
				""".formatted(senha);
	}
}
