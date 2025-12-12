package emer.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import emer.backend.entidades.CronogramaPdf;
import emer.backend.entidades.Prestamo;


public interface CronogramaPdfRepository extends JpaRepository<CronogramaPdf, Long> {
    Optional<CronogramaPdf> findByPrestamoId(Long prestamoId);
    Optional<CronogramaPdf> findByPrestamo(Prestamo prestamo);
}