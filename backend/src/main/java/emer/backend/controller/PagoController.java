package emer.backend.controller;

import emer.backend.service.interfaces.CuotaService;
import emer.backend.service.interfaces.PagoService;
import emer.backend.util.MercadoPagoService;
import emer.backend.entidades.PagoParcial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;


        @Autowired
        private CuotaService cuotaService;

        @Autowired
        private MercadoPagoService mercadoPagoService;

        @PostMapping("/mp/link")
        public ResponseEntity<?> crearLinkDePago(@RequestParam Long cuotaId) {
            System.out.println("🚀 ENTRANDO A crearLinkDePago");
            try {
                BigDecimal restante = cuotaService.obtenerRestanteCuota(cuotaId);

                if (restante.compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest().body("La cuota ya está completamente pagada.");
                }

                String link = mercadoPagoService.iniciarPagoParcialConMercadoPago(cuotaId, restante);

                return ResponseEntity.ok(Map.of(
                        "link", link,
                        "monto", restante
                ));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al generar link de pago: " + e.getMessage());
            }
        }
        @PostMapping("/parcial")
            public ResponseEntity<PagoParcial> registrarPagoParcial(
                    @RequestParam Long cuotaId,
                    @RequestParam String metodo,
                    @RequestParam BigDecimal monto,
                    @RequestParam(required = false) MultipartFile file
            ) {
                PagoParcial pago = pagoService.registrarPagoParcial(cuotaId, metodo, monto, file);
                return ResponseEntity.ok(pago);
            }
            @GetMapping("/cuota/{cuotaId}")
    public ResponseEntity<List<PagoParcial>> obtenerPagosPorCuota(@PathVariable Long cuotaId) {
        return ResponseEntity.ok(pagoService.obtenerPagosParcialesPorCuota(cuotaId));
    }
    @Autowired
    private emer.backend.repository.PagoParcialRepository pagoParcialRepository;
    @DeleteMapping("/parcial/{pagoId}")
    public ResponseEntity<Void> eliminarPagoParcial(@PathVariable Long pagoId) {
        pagoParcialRepository.deleteById(pagoId);
        return ResponseEntity.noContent().build();
    }

    // PagoController.java
        @PostMapping("/mp/confirmar")
public ResponseEntity<String> confirmarPagoDesdeCliente(
        @RequestParam String payment_id,
        @RequestParam Long cuotaId
) {
    boolean exito = mercadoPagoService.confirmarYActualizarPago(payment_id, cuotaId);
    if (exito) {
        return ResponseEntity.ok("Pago confirmado y registrado.");
    } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pago no aprobado o no registrado.");
    }
}



}
