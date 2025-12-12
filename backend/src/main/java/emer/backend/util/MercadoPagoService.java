package emer.backend.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import emer.backend.entidades.Cliente;
import emer.backend.entidades.ComprobantePdf;
import emer.backend.entidades.Cuota;
import emer.backend.entidades.PagoParcial;
import emer.backend.entidades.PagoParcial.EstadoPago;
import emer.backend.repository.ComprobantePdfRepository;
import emer.backend.repository.CuotaRepository;
import emer.backend.repository.PagoParcialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    private final CuotaRepository cuotaRepository;
    private final PagoParcialRepository pagoParcialRepository;
    private final ComprobantePdfRepository comprobantePdfRepository;

    public String iniciarPagoParcialConMercadoPago(Long cuotaId, BigDecimal monto) {
        System.out.println("🔄 ENTRANDO A iniciarPagoParcialConMercadoPago");
        System.out.println("CuotaId: " + cuotaId + ", Monto: " + monto);

        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        // 1. Guardar pago parcial pendiente
        PagoParcial pago = new PagoParcial();
        pago.setCuota(cuota);
        pago.setMetodoPago("MERCADOPAGO");
        pago.setMonto(monto);
        pago.setEstadoPago(EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDate.now());
        pago = pagoParcialRepository.save(pago);

        // 2. Crear preferencia
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> item = Map.of(
                "title", "Pago parcial cuota #" + cuota.getNumero(),
                "quantity", 1,
                "currency_id", "PEN",
                "unit_price", monto
        );

        Map<String, Object> metadata = Map.of(
                "cuota_id", cuotaId,
                "pago_parcial_id", pago.getId()
        );

        Map<String, Object> body = new HashMap<>();
        body.put("items", List.of(item));
        body.put("metadata", metadata);
        String frontBase = "https://prestamoscastillo.onrender.com/pago.html?cuotaId=" + cuotaId;

        body.put("back_urls", Map.of(
            "success", frontBase + "&status=approved",
            "failure", frontBase + "&status=rejected",
            "pending", frontBase + "&status=pending"
        ));

        body.put("notification_url", "https://backpracticaagile.onrender.com/api/mercadopago/webhook");
        body.put("auto_return", "approved");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.mercadopago.com/checkout/preferences",
                entity,
                Map.class
        );
        // 🔍 Verifica si MercadoPago respondió con error (403, 400, etc.)
        if (!response.getStatusCode().is2xxSuccessful()) {
        System.out.println("❌ Error al crear preferencia de MercadoPago:");
        System.out.println("🔑 Access Token: " + accessToken);
        System.out.println("🔢 Monto enviado: " + monto);
        System.out.println("📦 Body enviado: " + body);
        System.out.println("📥 Respuesta: " + response.getBody());
        System.out.println("🔁 Código HTTP: " + response.getStatusCode());
        throw new RuntimeException("Error al crear preferencia: " + response.getBody());
        }
        

        return response.getBody().get("sandbox_init_point").toString();
    }

    public void procesarNotificacionPago(String paymentId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> resp = restTemplate.exchange(
                "https://api.mercadopago.com/v1/payments/" + paymentId,
                HttpMethod.GET,
                entity,
                Map.class
        );

        String status = String.valueOf(resp.getBody().get("status"));

        if ("approved".equalsIgnoreCase(status)) {
            Map metadata = (Map) resp.getBody().get("metadata");
            Long pagoParcialId = Long.valueOf(String.valueOf(metadata.get("pago_parcial_id")));

            PagoParcial pago = pagoParcialRepository.findById(pagoParcialId)
                    .orElseThrow(() -> new RuntimeException("Pago parcial no encontrado"));

            pago.setEstadoPago(EstadoPago.CONFIRMADO);
            pagoParcialRepository.save(pago);

            // Verificar si la cuota ya está saldada
            Cuota cuota = pago.getCuota();
            List<PagoParcial> pagosConfirmados = pagoParcialRepository.findByCuotaId(cuota.getId())
                    .stream()
                    .filter(p -> p.getEstadoPago() == EstadoPago.CONFIRMADO)
                    .toList();

            BigDecimal totalPagado = pagosConfirmados.stream()
                    .map(PagoParcial::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal montoOriginal = cuota.getMonto();
            BigDecimal montoFinal = montoOriginal;

            if (cuota.estaAtrasada()) {
                long mesesAtrasados = ChronoUnit.MONTHS.between(cuota.getFechaPago(), LocalDate.now());
                if (mesesAtrasados > 0) {
                    BigDecimal interesMensual = new BigDecimal("1.25");
                    BigDecimal factorInteres = interesMensual.pow((int) mesesAtrasados);
                    montoFinal = montoOriginal.multiply(factorInteres).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
            }

            if (totalPagado.compareTo(montoFinal) >= 0) {
                cuota.setPagado(true);
                cuota.setFechaPagoReal(LocalDate.now());
                cuotaRepository.save(cuota);
            }

            // Generar comprobante
            generarComprobantePagoParcial(pago, cuota);
        }
    }

    private void generarComprobantePagoParcial(PagoParcial pago, Cuota cuota) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Cliente cliente = cuota.getPrestamo().getCliente();

            document.add(new Paragraph("COMPROBANTE DE PAGO PARCIAL"));
            document.add(new Paragraph("Empresa: PRESTAMOS CASTILLO"));
            document.add(new Paragraph("RUC: 20123456789"));
            document.add(new Paragraph("Dirección: Av. Los Héroes 456 - Trujillo"));
            document.add(new Paragraph("Teléfono: 044-123456"));
            document.add(new Paragraph("Email: contacto@prestamoscastillo.com"));
            document.add(new Paragraph("Cliente: " + cliente.getNombre()));
            document.add(new Paragraph("DNI/RUC: " + cliente.getDniRuc()));
            document.add(new Paragraph("Fecha de Pago: " + pago.getFechaPago()));
            document.add(new Paragraph("Monto Pagado: S/ " + pago.getMonto()));
            document.add(new Paragraph("Medio de Pago: " + pago.getMetodoPago()));
            document.add(new Paragraph("Cuota N°: " + cuota.getNumero()));

            String codigo = generarCodigoUnico();
            document.add(new Paragraph("N° Comprobante Parcial: " + codigo));
            document.close();

            ComprobantePdf comprobante = new ComprobantePdf();
            comprobante.setNumeroComprobante(codigo);
            comprobante.setNombreArchivo("comprobante_parcial_" + codigo + ".pdf");
            comprobante.setArchivoPdf(baos.toByteArray());
            comprobante.setCuota(cuota);

            pago.setNumeroComprobante(codigo);
            pago.setNombreArchivoPdf("comprobante_parcial_" + codigo + ".pdf");
            pagoParcialRepository.save(pago);
            comprobantePdfRepository.save(comprobante);

        } catch (Exception e) {
            throw new RuntimeException("Error generando comprobante parcial", e);
        }
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (comprobantePdfRepository.existsByNumeroComprobante(codigo));
        return codigo;
    }
    public boolean confirmarYActualizarPago(String paymentId, Long cuotaId) {
    try {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            "https://api.mercadopago.com/v1/payments/" + paymentId,
            HttpMethod.GET,
            entity,
            Map.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error al consultar pago en Mercado Pago");
        }

        Map<String, Object> json = response.getBody();
        String status = (String) json.get("status");

        if (!"approved".equalsIgnoreCase(status)) {
            return false; // No hacemos nada si no está aprobado
        }

        BigDecimal monto = new BigDecimal(String.valueOf(json.get("transaction_amount")));

        PagoParcial pago = new PagoParcial();
        pago.setCuota(cuotaRepository.findById(cuotaId).orElseThrow());
        pago.setMetodoPago("MERCADOPAGO");
        pago.setMonto(monto);
        pago.setEstadoPago(PagoParcial.EstadoPago.CONFIRMADO);
        pago.setFechaPago(LocalDate.now());

        pagoParcialRepository.save(pago);

        // Verifica si la cuota ya está saldada
        Cuota cuota = pago.getCuota();
        List<PagoParcial> pagosConfirmados = pagoParcialRepository.findByCuotaId(cuotaId)
            .stream()
            .filter(p -> p.getEstadoPago() == PagoParcial.EstadoPago.CONFIRMADO)
            .toList();

        BigDecimal totalPagado = pagosConfirmados.stream()
            .map(PagoParcial::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPagado.compareTo(cuota.getMontoFinal()) >= 0) {
            cuota.setPagado(true);
            cuota.setFechaPagoReal(LocalDate.now());
            cuotaRepository.save(cuota);
        }


        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}



}
