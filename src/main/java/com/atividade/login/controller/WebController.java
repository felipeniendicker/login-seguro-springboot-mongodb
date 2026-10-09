package com.atividade.login.controller;

import com.atividade.login.dto.CadastroUsuarioRequest;
import com.atividade.login.exception.EmailJaCadastradoException;
import com.atividade.login.model.Usuario;
import com.atividade.login.repository.UsuarioRepository;
import com.atividade.login.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

	private final UsuarioService usuarioService;
	private final UsuarioRepository usuarioRepository;

	public WebController(
			UsuarioService usuarioService,
			UsuarioRepository usuarioRepository) {
		this.usuarioService = usuarioService;
		this.usuarioRepository = usuarioRepository;
	}

	@GetMapping("/")
	public String inicio(Authentication authentication) {
		return estaAutenticado(authentication)
				? "redirect:/painel"
				: "redirect:/login";
	}

	@GetMapping("/login")
	public String login(Authentication authentication) {
		return estaAutenticado(authentication)
				? "redirect:/painel"
				: "login";
	}

	@GetMapping("/cadastro")
	public String cadastro(Model model) {
		if (!model.containsAttribute("cadastro")) {
			model.addAttribute(
					"cadastro",
					new CadastroUsuarioRequest("", "", ""));
		}
		return "cadastro";
	}

	@PostMapping("/cadastro")
	public String cadastrar(
			@Valid @ModelAttribute("cadastro") CadastroUsuarioRequest request,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "cadastro";
		}

		try {
			usuarioService.cadastrar(request);
		} catch (EmailJaCadastradoException exception) {
			bindingResult.rejectValue(
					"email",
					"email.duplicado",
					"Este e-mail já está cadastrado");
			return "cadastro";
		}

		redirectAttributes.addAttribute("cadastro", "sucesso");
		return "redirect:/login";
	}

	@GetMapping("/painel")
	public String painel(Authentication authentication) {
		if (possuiPerfil(authentication, "ROLE_ADMIN")) {
			return "redirect:/painel/admin";
		}
		if (possuiPerfil(authentication, "ROLE_MODERADOR")) {
			return "redirect:/painel/moderador";
		}
		return "redirect:/painel/usuario";
	}

	@GetMapping("/painel/usuario")
	public String painelUsuario(Authentication authentication, Model model) {
		adicionarUsuarioAoModelo(authentication, model);
		return "painel-usuario";
	}

	@GetMapping("/painel/moderador")
	public String painelModerador(Authentication authentication, Model model) {
		adicionarUsuarioAoModelo(authentication, model);
		return "painel-moderador";
	}

	@GetMapping("/painel/admin")
	public String painelAdmin(Authentication authentication, Model model) {
		adicionarUsuarioAoModelo(authentication, model);
		return "painel-admin";
	}

	@GetMapping("/acesso-negado")
	public String acessoNegado() {
		return "acesso-negado";
	}

	private void adicionarUsuarioAoModelo(
			Authentication authentication,
			Model model) {
		Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new IllegalStateException(
						"Usuário autenticado não encontrado"));
		model.addAttribute("nome", usuario.getNome());
		model.addAttribute("email", usuario.getEmail());
		model.addAttribute("perfil", usuario.getPerfil());
	}

	private boolean estaAutenticado(Authentication authentication) {
		return authentication != null
				&& authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken);
	}

	private boolean possuiPerfil(Authentication authentication, String perfil) {
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals(perfil));
	}
}
