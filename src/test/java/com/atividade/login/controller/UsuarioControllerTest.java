package com.atividade.login.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atividade.login.dto.CadastroUsuarioRequest;
import com.atividade.login.exception.EmailJaCadastradoException;
import com.atividade.login.model.Usuario;
import com.atividade.login.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

	@Mock
	private UsuarioService usuarioService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(new UsuarioController(usuarioService))
				.setControllerAdvice(new ValidationExceptionHandler())
				.build();
	}

	@Test
	void deveRetornarCriadoSemExporSenha() throws Exception {
		Usuario usuarioCadastrado = new Usuario();
		usuarioCadastrado.setNome("Usuário Teste");
		usuarioCadastrado.setEmail("usuario@exemplo.test");
		usuarioCadastrado.setSenha("hash-que-nao-deve-ser-retornado");
		usuarioCadastrado.setPerfil("USUARIO");
		when(usuarioService.cadastrar(any(CadastroUsuarioRequest.class)))
				.thenReturn(usuarioCadastrado);

		mockMvc.perform(post("/api/usuarios/cadastro")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "nome": "Usuário Teste",
							  "email": "usuario@exemplo.test",
							  "senha": "senha-segura-123"
							}
							"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Usuário Teste"))
				.andExpect(jsonPath("$.email").value("usuario@exemplo.test"))
				.andExpect(jsonPath("$.perfil").value("USUARIO"))
				.andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	void deveRetornarBadRequestParaDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/usuarios/cadastro")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "nome": " ",
							  "email": "email-invalido",
							  "senha": "123"
							}
							"""))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(usuarioService);
	}

	@Test
	void deveRetornarConflictParaEmailDuplicado() throws Exception {
		when(usuarioService.cadastrar(any(CadastroUsuarioRequest.class)))
				.thenThrow(new EmailJaCadastradoException());

		mockMvc.perform(post("/api/usuarios/cadastro")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "nome": "Usuário Teste",
							  "email": "usuario@exemplo.test",
							  "senha": "senha-segura-123"
							}
							"""))
				.andExpect(status().isConflict());
	}
}
