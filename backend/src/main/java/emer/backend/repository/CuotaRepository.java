package emer.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import emer.backend.entidades.Cuota;
import emer.backend.entidades.Prestamo;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {
    List<Cuota> findByPrestamo(Prestamo prestamo);
    @Query("SELECT c FROM Cuota c WHERE c.prestamo.cliente.id = :clienteId AND c.pagado = false AND c.fechaPago <= :hoy")
    List<Cuota> findDeudasVigentes(@Param("clienteId") Long clienteId, @Param("hoy") LocalDate hoy);
    @Query("SELECT c FROM Cuota c ORDER BY c.prestamo.id, c.numero")
    List<Cuota> findAllOrderByPrestamoIdAndNumero();
    @Query("SELECT c FROM Cuota c WHERE c.pagado = false AND MONTH(c.fechaPago) = MONTH(CURRENT_DATE) AND YEAR(c.fechaPago) = YEAR(CURRENT_DATE)")
    List<Cuota> findPendientesMesActual();

    // SOLUCIÓN DEFINITIVA: Consulta explícita para evitar ambigüedades
    @Query("SELECT c FROM Cuota c WHERE c.pagado = true AND c.fechaPago BETWEEN :startDate AND :endDate")
    List<Cuota> findCuotasPagadasEnRangoDeFechas(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
