package emer.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import emer.backend.entidades.PagoParcial;
import emer.backend.entidades.PagoParcial.EstadoPago;

public interface PagoParcialRepository extends JpaRepository<PagoParcial, Long> {
    List<PagoParcial> findByCuotaId(Long cuotaId);
    List<PagoParcial> findByCuotaIdAndEstadoPago(Long cuotaId, EstadoPago estadoPago);
    void deleteById(Long id);
    Optional<PagoParcial> findTopByCuotaIdAndMetodoPagoAndEstadoPagoOrderByIdDesc(
    Long cuotaId, String metodoPago, EstadoPago estadoPago);

}