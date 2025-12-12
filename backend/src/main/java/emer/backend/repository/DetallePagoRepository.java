package emer.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import emer.backend.entidades.DetallePago;

public interface DetallePagoRepository extends JpaRepository<DetallePago, Long> {
    List<DetallePago> findByCuotaId(Long cuotaId);
}