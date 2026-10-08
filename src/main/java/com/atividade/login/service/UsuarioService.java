package com.atividade.login.service;

import java.util.Locale;

import com.atividade.login.dto.CadastroUsuarioRequest;
import com.atividade.login.exception.EmailJaCadastradoException;
import com.atividade.login.model.Usuario;
import com.atividade.login.repository.UsuarioRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final String PERFIL_PADRAO = "USUARIO";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(CadastroUsuarioRequest request) {
        String emailNormalizado = normalizarEmail(request.email());

        if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(PERFIL_PADRAO);

        try {
            return usuarioRepository.save(usuario);
        } catch (DuplicateKeyException exception) {
            throw new EmailJaCadastradoException();
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
