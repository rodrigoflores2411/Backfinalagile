package emer.backend.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import emer.backend.dto.AuthResponse;
import emer.backend.dto.CambiarPasswordRequest;
import emer.backend.dto.CambiarUsernameRequest;
import emer.backend.dto.LoginRequest;
import emer.backend.entidades.Usuario;
import emer.backend.repository.UsuarioRepository;
import emer.backend.security.JwtUtil;
import emer.backend.service.interfaces.UsuarioService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        System.out.println("Intento de login para: " + request.getUsername());

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("Contraseña enviada: " + request.getPassword());
        System.out.println("Contraseña encriptada en BD: " + usuario.getPassword());
        System.out.println("¿Coinciden? " + passwordEncoder.matches(request.getPassword(), usuario.getPassword()));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtUtil.generateToken(usuario.getUsername());
        return new AuthResponse(token);
    }

    @Override
    public void cambiarUsername(CambiarUsernameRequest request) {
        String actualUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(actualUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setUsername(request.getNuevoUsername());
        usuarioRepository.save(usuario);
    }

    @Override
    public void cambiarPassword(CambiarPasswordRequest request) {
        String actualUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(actualUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }
}
