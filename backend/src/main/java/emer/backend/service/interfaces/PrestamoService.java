package emer.backend.service.interfaces;

import emer.backend.dto.PrestamoRequest;
import emer.backend.entidades.Prestamo;
import java.math.BigDecimal;
import java.util.List;

public interface PrestamoService {
    void crearPrestamo(PrestamoRequest request);
    List<Prestamo> obtenerPrestamosPorCliente(String dniRuc);
    List<Prestamo> obtenerTodosLosPrestamos();
    BigDecimal obtenerTotalMensual(String dniRuc); // NUEVO MÉTODO
}
