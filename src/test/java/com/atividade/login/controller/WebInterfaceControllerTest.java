package com.atividade.login.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import com.atividade.login.config.SecurityConfig;
import com.atividade.login.dto.CadastroUsuarioRequest;
import com.atividade.login.exception.EmailJaCadastradoException;
import com.atividade.login.model.Usuario;
import com.atividade.login.repository.UsuarioRepository;
import com.atividade.login.service.UsuarioDetailsService;
import com.atividade.login.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(WebController.class)
@Import({SecurityConfig.class, UsuarioDetailsService.class})
class WebInterfaceControllerTest {

	private static final String EMAIL = "usuario@exemplo.test";
	private static final String SENHA = "senha-segura-123";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@Test
	void deveExibirLoginComCsrf() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(content().string(containsString("name=\"_csrf\"")));
	}

	@Test
	void deveMostrarMensagemParaLoginInvalido() throws Exception {
		when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

		mockMvc.perform(post("/login")
					.with(csrf())
					.param("email", EMAIL)
					.param("senha", "senha-incorreta"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?erro"));

		mockMvc.perform(get("/login").queryParam("erro"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("E-mail ou senha inválidos")));
	}

	@Test
	void deveCadastrarPelaInterface() throws Exception {
		mockMvc.perform(post("/cadastro")
					.with(csrf())
					.param("nome", "Usuário Teste")
					.param("email", EMAIL)
					.param("senha", SENHA))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?cadastro=sucesso"));

		verify(usuarioService).cadastrar(any(CadastroUsuarioRequest.class));
	}

	@Test
	void deveMostrarErrosDeValidacaoNoCadastro() throws Exception {
		mockMvc.perform(post("/cadastro")
					.with(csrf())
					.param("nome", " ")
					.param("email", "email-invalido")
					.param("senha", "123"))
				.andExpect(status().isOk())
				.andExpect(view().name("cadastro"))
				.andExpect(content().string(containsString("Nome é obrigatório")));
	}

	@Test
	void deveMostrarMensagemParaEmailDuplicado() throws Exception {
		when(usuarioService.cadastrar(any(CadastroUsuarioRequest.class)))
				.thenThrow(new EmailJaCadastradoException());

		mockMvc.perform(post("/cadastro")
					.with(csrf())
					.param("nome", "Usuário Teste")
					.param("email", EMAIL)
					.param("senha", SENHA))
				.andExpect(status().isOk())
				.andExpect(view().name("cadastro"))
				.andExpect(content().string(
						containsString("Este e-mail já está cadastrado")));
	}

	@Test
	void deveAutenticarUsuarioERedirecionarParaSeuPainel() throws Exception {
		MockHttpSession session = autenticar("USUARIO");

		mockMvc.perform(get("/painel").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/painel/usuario"));
	}

	@Test
	void deveRedirecionarModeradorParaSeuPainel() throws Exception {
		MockHttpSession session = autenticar("MODERADOR");

		mockMvc.perform(get("/painel").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/painel/moderador"));
	}

	@Test
	void deveRedirecionarAdminParaSeuPainel() throws Exception {
		MockHttpSession session = autenticar("ADMIN");

		mockMvc.perform(get("/painel").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/painel/admin"));
	}

	@Test
	void deveEncerrarSessaoPelaInterface() throws Exception {
		MockHttpSession session = autenticar("USUARIO");

		mockMvc.perform(post("/api/auth/logout")
					.param("web", "true")
					.with(csrf())
					.session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?logout"));

		assertTrue(session.isInvalid());
	}

	@Test
	void deveRedirecionarVisitanteParaLogin() throws Exception {
		mockMvc.perform(get("/painel/usuario"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	@WithMockUser(username = EMAIL, roles = "USUARIO")
	void deveExibirAcessoNegadoAoUsuarioSemPermissao() throws Exception {
		mockMvc.perform(get("/painel/admin"))
				.andExpect(status().isForbidden())
				.andExpect(forwardedUrl("/acesso-negado"));
	}

	@Test
	@WithMockUser(username = EMAIL, roles = "USUARIO")
	void deveRenderizarPainelDeUsuario() throws Exception {
		when(usuarioRepository.findByEmail(EMAIL))
				.thenReturn(Optional.of(criarUsuario("USUARIO")));

		mockMvc.perform(get("/painel/usuario"))
				.andExpect(status().isOk())
				.andExpect(view().name("painel-usuario"))
				.andExpect(content().string(containsString("Usuário Teste")));
	}

	@Test
	@WithMockUser(username = EMAIL, roles = "MODERADOR")
	void deveRenderizarPainelDeModerador() throws Exception {
		when(usuarioRepository.findByEmail(EMAIL))
				.thenReturn(Optional.of(criarUsuario("MODERADOR")));

		mockMvc.perform(get("/painel/moderador"))
				.andExpect(status().isOk())
				.andExpect(view().name("painel-moderador"))
				.andExpect(content().string(containsString("Área de moderação")));
	}

	@Test
	@WithMockUser(username = EMAIL, roles = "ADMIN")
	void deveRenderizarPainelDeAdmin() throws Exception {
		when(usuarioRepository.findByEmail(EMAIL))
				.thenReturn(Optional.of(criarUsuario("ADMIN")));

		mockMvc.perform(get("/painel/admin"))
				.andExpect(status().isOk())
				.andExpect(view().name("painel-admin"))
				.andExpect(content().string(containsString("Área administrativa")));
	}

	private MockHttpSession autenticar(String perfil) throws Exception {
		when(usuarioRepository.findByEmail(EMAIL))
				.thenReturn(Optional.of(criarUsuario(perfil)));

		MvcResult result = mockMvc.perform(post("/login")
					.with(csrf())
					.param("email", EMAIL)
					.param("senha", SENHA))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/painel"))
				.andReturn();

		MockHttpSession session =
				(MockHttpSession) result.getRequest().getSession(false);
		assertNotNull(session);
		return session;
	}

	private Usuario criarUsuario(String perfil) {
		Usuario usuario = new Usuario();
		usuario.setNome("Usuário Teste");
		usuario.setEmail(EMAIL);
		usuario.setSenha(new BCryptPasswordEncoder().encode(SENHA));
		usuario.setPerfil(perfil);
		return usuario;
	}
}
