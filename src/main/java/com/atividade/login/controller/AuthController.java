package com.atividade.login.controller;

import java.util.Locale;

import com.atividade.login.dto.CsrfTokenResponse;
import com.atividade.login.dto.LoginRequest;
import com.atividade.login.dto.UsuarioResponse;
import com.atividade.login.model.Usuario;
import com.atividade.login.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
	private final UsuarioRepository usuarioRepository;

	public AuthController(
			AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			SessionAuthenticationStrategy sessionAuthenticationStrategy,
			UsuarioRepository usuarioRepository) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
		this.usuarioRepository = usuarioRepository;
	}

	@GetMapping("/csrf")
	public CsrfTokenResponse csrf(CsrfToken csrfToken) {
		return new CsrfTokenResponse(
				csrfToken.getHeaderName(),
				csrfToken.getParameterName(),
				csrfToken.getToken());
	}

	@PostMapping("/login")
	public UsuarioResponse login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		String emailNormalizado = normalizarEmail(request.email());

		try {
			Authentication authentication = authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken.unauthenticated(
							emailNormalizado,
							request.senha()));
			Usuario usuario = buscarUsuario(emailNormalizado);
			sessionAuthenticationStrategy.onAuthentication(
					authentication,
					httpRequest,
					httpResponse);

			SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
			securityContext.setAuthentication(authentication);
			SecurityContextHolder.setContext(securityContext);
			securityContextRepository.saveContext(
					securityContext,
					httpRequest,
					httpResponse);

			return UsuarioResponse.from(usuario);
		} catch (AuthenticationException exception) {
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED,
					"Credenciais inválidas");
		}
	}

	@GetMapping("/me")
	public UsuarioResponse me(Authentication authentication) {
		return UsuarioResponse.from(buscarUsuario(authentication.getName()));
	}

	private Usuario buscarUsuario(String email) {
		return usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.UNAUTHORIZED,
						"Usuário autenticado não encontrado"));
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
