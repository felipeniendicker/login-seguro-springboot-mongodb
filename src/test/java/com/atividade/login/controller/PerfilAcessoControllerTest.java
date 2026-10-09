package com.atividade.login.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.atividade.login.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PainelController.class)
@Import(SecurityConfig.class)
class PerfilAcessoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void deveRetornarUnauthorizedNosPaineisSemAutenticacao() throws Exception {
		List<String> paineis = List.of(
				"/api/usuario/painel",
				"/api/moderador/painel",
				"/api/admin/painel");

		for (String painel : paineis) {
			mockMvc.perform(get(painel))
					.andExpect(status().isUnauthorized());
		}
	}

	@Test
	@WithMockUser(roles = "USUARIO")
	void usuarioDeveAcessarPainelDeUsuario() throws Exception {
		mockMvc.perform(get("/api/usuario/painel"))
				.andExpect(status().isOk())
				.andExpect(content().string("Painel do usuário"));
	}

	@Test
	@WithMockUser(roles = "USUARIO")
	void usuarioNaoDeveAcessarPaineisPrivilegiados() throws Exception {
		mockMvc.perform(get("/api/moderador/painel"))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/admin/painel"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "MODERADOR")
	void moderadorDeveAcessarPaineisDeUsuarioEModerador() throws Exception {
		mockMvc.perform(get("/api/usuario/painel"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/moderador/painel"))
				.andExpect(status().isOk())
				.andExpect(content().string("Painel do moderador"));
	}

	@Test
	@WithMockUser(roles = "MODERADOR")
	void moderadorNaoDeveAcessarPainelDeAdmin() throws Exception {
		mockMvc.perform(get("/api/admin/painel"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminDeveAcessarTodosOsPaineis() throws Exception {
		mockMvc.perform(get("/api/usuario/painel"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/moderador/painel"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/painel"))
				.andExpect(status().isOk())
				.andExpect(content().string("Painel do administrador"));
	}
}
