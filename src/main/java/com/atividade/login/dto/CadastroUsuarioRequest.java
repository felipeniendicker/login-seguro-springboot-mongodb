package com.atividade.login.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest(
		@NotBlank(message = "Nome é obrigatório")
		String nome,

		@NotBlank(message = "E-mail é obrigatório")
		@Email(message = "E-mail deve ter formato válido")
		String email,

		@NotBlank(message = "Senha é obrigatória")
		@Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
		String senha) {

	@Override
	public String toString() {
		return "CadastroUsuarioRequest[nome=" + nome
				+ ", email=" + email
				+ ", senha=[PROTEGIDA]]";
	}
}
