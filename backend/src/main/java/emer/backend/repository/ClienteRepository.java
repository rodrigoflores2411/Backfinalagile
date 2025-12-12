package emer.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import emer.backend.entidades.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDniRuc(String dniRuc);
}