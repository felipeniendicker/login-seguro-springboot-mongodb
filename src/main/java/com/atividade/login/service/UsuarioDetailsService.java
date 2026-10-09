package com.atividade.login.service;

import java.util.Locale;

import com.atividade.login.model.Usuario;
import com.atividade.login.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
		Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
				.orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));

		return User.withUsername(usuario.getEmail())
				.password(usuario.getSenha())
				.authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil()))
				.build();
	}
}
