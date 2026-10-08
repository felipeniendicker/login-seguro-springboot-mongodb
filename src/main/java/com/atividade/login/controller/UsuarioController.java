package com.atividade.login.controller;

import com.atividade.login.dto.CadastroUsuarioRequest;
import com.atividade.login.dto.UsuarioResponse;
import com.atividade.login.model.Usuario;
import com.atividade.login.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping("/cadastro")
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioResponse cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
		Usuario usuarioCadastrado = usuarioService.cadastrar(request);
		return UsuarioResponse.from(usuarioCadastrado);
	}
}
