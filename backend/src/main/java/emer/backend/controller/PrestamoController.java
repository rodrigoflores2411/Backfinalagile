package emer.backend.controller;

import emer.backend.dto.PrestamoDTO;
import emer.backend.dto.PrestamoRequest;
import emer.backend.entidades.Prestamo;
import emer.backend.service.interfaces.PrestamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PostMapping
    public ResponseEntity<String> registrarPrestamo(@RequestBody PrestamoRequest request) {
        prestamoService.crearPrestamo(request);
        return ResponseEntity.ok("Préstamo creado correctamente");
    }

    @GetMapping("/historial/{dniRuc}")
    public ResponseEntity<List<Prestamo>> historial(@PathVariable String dniRuc) {
        return ResponseEntity.ok(prestamoService.obtenerPrestamosPorCliente(dniRuc));
    }

    @GetMapping("/historial")
public ResponseEntity<List<PrestamoDTO>> historialCompleto() {
    List<Prestamo> prestamos = prestamoService.obtenerTodosLosPrestamos();
    List<PrestamoDTO> resultado = prestamos.stream()
        .map(PrestamoDTO::new)
        .toList();
    return ResponseEntity.ok(resultado);
}

    @GetMapping("/totales/{dniRuc}")
    public ResponseEntity<BigDecimal> obtenerTotalMensual(@PathVariable String dniRuc) {
        return ResponseEntity.ok(prestamoService.obtenerTotalMensual(dniRuc));
    }
}

