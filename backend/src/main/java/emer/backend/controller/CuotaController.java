package emer.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import emer.backend.dto.DetallePagoDTO;
import emer.backend.entidades.ComprobantePdf;
import emer.backend.entidades.Cuota;
import emer.backend.entidades.PagoParcial;
import emer.backend.service.interfaces.CuotaService;
import emer.backend.service.interfaces.PagoService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import emer.backend.repository.ComprobantePdfRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/cuotas")
public class CuotaController {

    private final CuotaService cuotaService;

    private final emer.backend.repository.CuotaRepository cuotaRepository;

    public CuotaController(CuotaService cuotaService, emer.backend.repository.CuotaRepository cuotaRepository) {
        this.cuotaService = cuotaService;
        this.cuotaRepository = cuotaRepository;
    }
    @GetMapping("/deudas/{idCliente}")
    public ResponseEntity<?> obtenerDeudas(@PathVariable Long idCliente) {
        try {
            List<Cuota> deudas = cuotaService.obtenerDeudasCliente(idCliente);
            return ResponseEntity.ok(deudas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }
        @GetMapping("/pendientes")
        public List<Cuota> obtenerCuotasPendientes() {
            List<Cuota> cuotas = cuotaService.obtenerCuotasPendientes();
            System.out.println("Total cuotas pendientes encontradas: " + cuotas.size());
            return cuotas;
        }
    @GetMapping("/pendientes/mes-actual")
    public ResponseEntity<List<Cuota>> getPendientesMesActual() {
        return ResponseEntity.ok(cuotaRepository.findPendientesMesActual());
    }
    @Autowired
    private PagoService pagoService;
    @GetMapping("/cuota/{cuotaId}")
    public ResponseEntity<?> listarPagosParcialesPorCuota(@PathVariable Long cuotaId) {
        try {
            List<PagoParcial> pagos = pagoService.obtenerPagosParcialesPorCuota(cuotaId);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener pagos parciales: " + e.getMessage());
        }
    }
    @GetMapping("/{cuotaId}/restante")
        public ResponseEntity<BigDecimal> obtenerRestante(@PathVariable Long cuotaId) {
            BigDecimal restante = cuotaService.obtenerRestanteCuota(cuotaId);
            return ResponseEntity.ok(restante);
        }

                @GetMapping("/detalle/{cuotaId}")
        public ResponseEntity<?> obtenerCuotaPorId(@PathVariable Long cuotaId) {
            try {
                Optional<Cuota> cuota = cuotaService.buscarPorId(cuotaId);
                if (cuota.isPresent()) {
                    return ResponseEntity.ok(cuota.get());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cuota no encontrada");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la cuota: " + e.getMessage());
            }
        }
            @PostMapping("/{cuotaId}/pago")
        public ResponseEntity<Void> pagarCuota(
                @PathVariable Long cuotaId,
                @RequestBody List<DetallePagoDTO> pagos
        ) {
            cuotaService.pagarCuota(cuotaId, pagos);
            return ResponseEntity.ok().build();
        }
        @PostMapping("/{cuotaId}/cerrar")
        public ResponseEntity<Void> cerrarCuota(@PathVariable Long cuotaId) {
            cuotaService.verificarYGenerarComprobanteFinal(cuotaId);
            return ResponseEntity.ok().build();
        }
        @Autowired
        private ComprobantePdfRepository comprobantePdfRepository;
        @GetMapping("/comprobantes/cuota/{cuotaId}")
        public ResponseEntity<byte[]> descargarComprobante(@PathVariable Long cuotaId) {
            ComprobantePdf comprobante = comprobantePdfRepository.findByCuotaId(cuotaId)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado para la cuota " + cuotaId));

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + comprobante.getNombreArchivo())
                .contentType(MediaType.APPLICATION_PDF)
                .body(comprobante.getArchivoPdf());
        }



}



