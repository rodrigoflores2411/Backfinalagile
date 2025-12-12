
package emer.backend;
import emer.backend.entidades.Cliente;
import emer.backend.entidades.Cuota;
import emer.backend.entidades.Prestamo;
import emer.backend.repository.CuotaRepository;
import emer.backend.service.impl.CuadreCajaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CuadreCajaServiceTest {

    @Mock
    private CuotaRepository cuotaRepository;

    @InjectMocks
    private CuadreCajaServiceImpl cuadreCajaService;

    @Test
    void testGenerarCuadreCajaExcel_ConDatosValidos() {
        // 1. Preparación (Arrange)
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente de Prueba");

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);

        Cuota cuota = new Cuota();
        cuota.setPrestamo(prestamo);
        cuota.setNumero(1);
        cuota.setMonto(new BigDecimal("150.00"));
        cuota.setPagado(true);
        cuota.setFechaPago(LocalDate.of(2024, 5, 15));

        // Simular que el repositorio devuelve nuestra cuota de prueba
        when(cuotaRepository.findCuotasPagadasEnRangoDeFechas(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(cuota));

        // 2. Actuación (Act)
        ByteArrayInputStream resultado = null;
        Exception exception = null;
        try {
            resultado = cuadreCajaService.generarCuadreCajaExcel(2024, 5);
        } catch (Exception e) {
            exception = e;
        }


        // 3. Aserción (Assert)
        assertNull(exception, "El método no debería lanzar una excepción con datos válidos. Error: " + exception);
        assertNotNull(resultado, "El resultado no debería ser nulo.");
        assertTrue(resultado.available() > 0, "El stream del Excel debería contener datos.");
    }

    @Test
    void testGenerarCuadreCajaExcel_ConMontoNulo() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente Monto Nulo");
        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);

        Cuota cuotaConMontoNulo = new Cuota();
        cuotaConMontoNulo.setPrestamo(prestamo);
        cuotaConMontoNulo.setNumero(2);
        cuotaConMontoNulo.setMonto(null); // <--- El problema que sospechamos
        cuotaConMontoNulo.setPagado(true);
        cuotaConMontoNulo.setFechaPago(LocalDate.of(2024, 5, 20));

        when(cuotaRepository.findCuotasPagadasEnRangoDeFechas(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(cuotaConMontoNulo));

        // Act
        ByteArrayInputStream resultado = null;
        Exception exception = null;
        try {
            resultado = cuadreCajaService.generarCuadreCajaExcel(2024, 5);
        } catch (Exception e) {
            exception = e;
        }

        // Assert
        assertNull(exception, "El método NO debería lanzar una excepción incluso con monto nulo. Error: " + exception);
        assertNotNull(resultado, "El Excel debería generarse incluso si un monto es nulo.");
    }
}
