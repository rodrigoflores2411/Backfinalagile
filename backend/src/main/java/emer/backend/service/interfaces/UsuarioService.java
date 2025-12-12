package emer.backend.service.interfaces;

import emer.backend.dto.*;
import emer.backend.dto.AuthResponse;
public interface UsuarioService {
    AuthResponse login(LoginRequest request);
    
    void cambiarUsername(CambiarUsernameRequest request);

    void cambiarPassword(CambiarPasswordRequest request);
}