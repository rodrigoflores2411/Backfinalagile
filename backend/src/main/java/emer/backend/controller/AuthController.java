package emer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import emer.backend.dto.AuthResponse;
import emer.backend.dto.CambiarPasswordRequest;
import emer.backend.dto.CambiarUsernameRequest;
import emer.backend.dto.LoginRequest;
import emer.backend.service.interfaces.UsuarioService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }
    @PostMapping("/cambiar-usuario")
public ResponseEntity<String> cambiarUsuario(@RequestBody CambiarUsernameRequest request) {
    usuarioService.cambiarUsername(request);
    return ResponseEntity.ok("Usuario actualizado");
}

@PostMapping("/cambiar-password")
public ResponseEntity<String> cambiarPassword(@RequestBody CambiarPasswordRequest request) {
    usuarioService.cambiarPassword(request);
    return ResponseEntity.ok("Contraseña actualizada");
}

}
