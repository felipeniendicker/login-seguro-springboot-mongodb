package com.atividade.login.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.atividade.login.dto.CadastroUsuarioRequest;
import com.atividade.login.exception.EmailJaCadastradoException;
import com.atividade.login.model.Usuario;
import com.atividade.login.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	private static final String SENHA_ORIGINAL = "senha-segura-123";

	@Mock
	private UsuarioRepository usuarioRepository;

	private BCryptPasswordEncoder passwordEncoder;
	private UsuarioService usuarioService;

	@BeforeEach
	void setUp() {
		passwordEncoder = new BCryptPasswordEncoder();
		usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
	}

	@Test
	void deveCadastrarComEmailNormalizadoSenhaProtegidaEPerfilPadrao() {
		CadastroUsuarioRequest request = new CadastroUsuarioRequest(
				"  Usuário Teste  ",
				"  USUARIO.TESTE@EXEMPLO.TEST  ",
				SENHA_ORIGINAL);
		when(usuarioRepository.findByEmail("usuario.teste@exemplo.test"))
				.thenReturn(Optional.empty());
		when(usuarioRepository.save(any(Usuario.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Usuario usuarioCadastrado = usuarioService.cadastrar(request);

		ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(usuarioCaptor.capture());
		Usuario usuarioSalvo = usuarioCaptor.getValue();

		assertEquals("Usuário Teste", usuarioCadastrado.getNome());
		assertEquals("usuario.teste@exemplo.test", usuarioCadastrado.getEmail());
		assertEquals("USUARIO", usuarioCadastrado.getPerfil());
		assertNotEquals(SENHA_ORIGINAL, usuarioSalvo.getSenha());
		assertTrue(passwordEncoder.matches(SENHA_ORIGINAL, usuarioSalvo.getSenha()));
	}

	@Test
	void deveRejeitarEmailJaCadastrado() {
		CadastroUsuarioRequest request = criarRequestValido();
		when(usuarioRepository.findByEmail("usuario@exemplo.test"))
				.thenReturn(Optional.of(new Usuario()));

		assertThrows(
				EmailJaCadastradoException.class,
				() -> usuarioService.cadastrar(request));

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void deveTratarViolacaoDoIndiceUnicoComoEmailDuplicado() {
		CadastroUsuarioRequest request = criarRequestValido();
		when(usuarioRepository.findByEmail("usuario@exemplo.test"))
				.thenReturn(Optional.empty());
		when(usuarioRepository.save(any(Usuario.class)))
				.thenThrow(new DuplicateKeyException("Índice único violado"));

		assertThrows(
				EmailJaCadastradoException.class,
				() -> usuarioService.cadastrar(request));
	}

	private CadastroUsuarioRequest criarRequestValido() {
		return new CadastroUsuarioRequest(
				"Usuário Teste",
				"usuario@exemplo.test",
				SENHA_ORIGINAL);
	}
}
