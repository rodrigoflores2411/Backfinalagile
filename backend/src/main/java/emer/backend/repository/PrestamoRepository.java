package emer.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import emer.backend.entidades.Cliente;
import emer.backend.entidades.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findAllByClienteOrderByFechaDesc(Cliente cliente);
    List<Prestamo> findAllByOrderByFechaDesc();
    
    // Buscar préstamos del cliente en un día específico
    @Query("SELECT p FROM Prestamo p WHERE p.cliente = :cliente AND p.fecha = :fecha")
    List<Prestamo> findByClienteAndFecha(@Param("cliente") Cliente cliente, @Param("fecha") LocalDate fecha);

    // Buscar préstamos del cliente en un mes específico
    @Query("SELECT p FROM Prestamo p WHERE p.cliente = :cliente AND MONTH(p.fecha) = :mes AND YEAR(p.fecha) = :anio")
    List<Prestamo> findByClienteInMes(@Param("cliente") Cliente cliente, @Param("mes") int mes, @Param("anio") int anio);

    

}
