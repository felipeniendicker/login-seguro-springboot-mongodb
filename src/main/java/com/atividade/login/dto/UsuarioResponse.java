package com.atividade.login.dto;

import com.atividade.login.model.Usuario;

public record UsuarioResponse(
		String id,
		String nome,
		String email,
		String perfil) {

	public static UsuarioResponse from(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getPerfil());
	}
}
