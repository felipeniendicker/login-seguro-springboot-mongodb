package com.atividade.login.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PainelController {

	@GetMapping("/usuario/painel")
	public String painelUsuario() {
		return "Painel do usuário";
	}

	@GetMapping("/moderador/painel")
	public String painelModerador() {
		return "Painel do moderador";
	}

	@GetMapping("/admin/painel")
	public String painelAdmin() {
		return "Painel do administrador";
	}
}
