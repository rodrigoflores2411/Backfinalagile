package emer.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import emer.backend.entidades.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}