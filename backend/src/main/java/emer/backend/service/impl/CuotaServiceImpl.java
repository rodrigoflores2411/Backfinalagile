package emer.backend.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import emer.backend.dto.DetallePagoDTO;
import emer.backend.entidades.Cliente;
import emer.backend.entidades.ComprobantePdf;
import emer.backend.entidades.Cuota;
import emer.backend.entidades.PagoParcial;
import emer.backend.entidades.PagoParcial.EstadoPago;
import emer.backend.repository.CuotaRepository;
import emer.backend.service.interfaces.CuotaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
@Service
public class CuotaServiceImpl implements CuotaService{
    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private emer.backend.repository.ComprobantePdfRepository comprobantePdfRepository;

    @Autowired
    private emer.backend.repository.PagoParcialRepository pagoParcialRepository;


        public List<Cuota> obtenerDeudasCliente(Long clienteId) {
            LocalDate hoy = LocalDate.now();
            return cuotaRepository.findDeudasVigentes(clienteId, hoy);
        }

        @Override
        public List<Cuota> obtenerCuotasPendientes() {
            return cuotaRepository.findAllOrderByPrestamoIdAndNumero();
                }
        @Override
        public void pagarCuota(Long cuotaId, List<DetallePagoDTO> pagos) {
            Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

            if (cuota.isPagado()) {
                throw new IllegalStateException("La cuota ya fue pagada.");
            }

            // Registrar cada pago parcial
            for (DetallePagoDTO pago : pagos) {
                PagoParcial entidad = new PagoParcial();
                entidad.setCuota(cuota);
                entidad.setMetodoPago(pago.getMedioPago());
                entidad.setMonto(pago.getMonto());
                entidad.setFechaPago(LocalDate.now());
                entidad.setComprobanteUrl(pago.getComprobanteUrl()); // null si es efectivo
                entidad.setEstadoPago(EstadoPago.CONFIRMADO); // puedes manejar lógica más compleja si deseas

                pagoParcialRepository.save(entidad);
            }

            // Calcular total pagado
            List<PagoParcial> pagosConfirmados = pagoParcialRepository.findByCuotaId(cuotaId).stream()
                .filter(p -> p.getEstadoPago() == EstadoPago.CONFIRMADO)
                .toList();

            BigDecimal totalPagado = pagosConfirmados.stream()
                .map(PagoParcial::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal montoFinal = cuota.getMontoFinal() != null
                ? cuota.getMontoFinal()
                : cuota.getMonto();

            // Si ya se cubrió el montoFinal => generar comprobante único
            if (totalPagado.compareTo(montoFinal) >= 0) {
                String numeroComprobante = generarCodigoUnico();

                cuota.setPagado(true);
                cuota.setFechaPagoReal(LocalDate.now());
                cuota.setComprobante(numeroComprobante);
                cuotaRepository.save(cuota);

                // Generar PDF
                byte[] archivoPdf = generarPdfConDetalleMedios(pagosConfirmados, cuota, numeroComprobante);

                ComprobantePdf comprobantePdf = new ComprobantePdf();
                comprobantePdf.setNumeroComprobante(numeroComprobante);
                comprobantePdf.setNombreArchivo("comprobante_total_" + cuota.getId() + ".pdf");
                comprobantePdf.setArchivoPdf(archivoPdf);
                comprobantePdf.setCuota(cuota);

                comprobantePdfRepository.save(comprobantePdf);
            }
        }


private byte[] generarPdfConDetalleMedios(List<PagoParcial> pagos, Cuota cuota, String numeroComprobante) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        Cliente cliente = cuota.getPrestamo().getCliente();
        String dniRuc = cliente.getDniRuc();
        boolean esFactura = dniRuc != null && dniRuc.trim().length() == 11;
        boolean esBoleta = dniRuc != null && dniRuc.trim().length() == 8;

        // Datos del emisor
        agregarTextoCentrado(document, emer.backend.entidades.EmisorDatos.NOMBRE_EMPRESA);
        agregarTextoCentrado(document, "RUC: " + emer.backend.entidades.EmisorDatos.RUC);
        agregarTextoCentrado(document, emer.backend.entidades.EmisorDatos.DIRECCION);
        agregarTextoCentrado(document, "Teléfono: " + emer.backend.entidades.EmisorDatos.TELEFONO);
        agregarTextoCentrado(document, "Email: " + emer.backend.entidades.EmisorDatos.EMAIL);

        document.add(new Paragraph(" "));

        // Tipo de comprobante
        if (esFactura) {
            agregarTextoCentrado(document, "FACTURA ELECTRÓNICA");
        } else if (esBoleta) {
            agregarTextoCentrado(document, "BOLETA DE VENTA ELECTRÓNICA");
        } else {
            agregarTextoCentrado(document, "COMPROBANTE DE PAGO");
        }

        document.add(new Paragraph(" "));

        // Datos del cliente
        agregarTextoCentrado(document, "Cliente: " + cliente.getNombre());
        agregarTextoCentrado(document, (esFactura ? "RUC: " : "DNI: ") + cliente.getDniRuc());

        if (esFactura && cliente.getDireccion() != null) {
            agregarTextoCentrado(document, "Dirección: " + cliente.getDireccion());
        }

        document.add(new Paragraph(" "));

        // Fecha, cuota, comprobante
        agregarTextoCentrado(document, "Fecha de Pago: " + LocalDate.now());
        agregarTextoCentrado(document, "Cuota N°: " + cuota.getNumero());

        BigDecimal subtotal = cuota.getMontoFinal();
        BigDecimal igv = BigDecimal.ZERO;
        BigDecimal total = subtotal;

        if (esFactura) {
            // Se asume que montoFinal incluye IGV
            BigDecimal divisor = new BigDecimal("1.18");
            subtotal = cuota.getMontoFinal().divide(divisor, 2, RoundingMode.HALF_UP);
            igv = cuota.getMontoFinal().subtract(subtotal);

            agregarTextoCentrado(document, "Subtotal: S/ " + subtotal);
            agregarTextoCentrado(document, "IGV (18%): S/ " + igv);
            agregarTextoCentrado(document, "Total: S/ " + total);
        } else {
            agregarTextoCentrado(document, "Monto Final: S/ " + cuota.getMontoFinal());
        }

        if (cuota.getIntereses() != null && cuota.getIntereses().compareTo(BigDecimal.ZERO) > 0) {
            agregarTextoCentrado(document, "Incluye intereses por mora: S/ " + cuota.getIntereses());
        }

        document.add(new Paragraph(" "));

        // Detalle de pagos
        agregarTextoCentrado(document, "DETALLE DE PAGOS:");
        for (PagoParcial pago : pagos) {
            agregarTextoCentrado(document, "- " + pago.getMetodoPago() + ": S/ " + pago.getMonto());
        }

        document.add(new Paragraph(" "));

        agregarTextoCentrado(document, "N° Comprobante: " + numeroComprobante);
        document.close();

        return baos.toByteArray();

    } catch (Exception e) {
        throw new RuntimeException("Error generando el comprobante PDF", e);
    }
}

// Método auxiliar para centrar texto
private void agregarTextoCentrado(Document document, String texto) throws DocumentException {
    Paragraph p = new Paragraph(texto);
    p.setAlignment(Paragraph.ALIGN_CENTER);
    document.add(p);
}





    private String generarCodigoUnico() {
    String codigo;
    do {
        codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("Código generado: " + codigo);  // <--- AÑADE ESTO
    } while (comprobantePdfRepository.existsByNumeroComprobante(codigo));
    return codigo;
}

        @Override
public BigDecimal obtenerRestanteCuota(Long cuotaId) {
    Cuota cuota = cuotaRepository.findById(cuotaId)
            .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

    List<PagoParcial> pagosConfirmados = pagoParcialRepository.findByCuotaId(cuotaId)
            .stream()
            .filter(p -> EstadoPago.CONFIRMADO == p.getEstadoPago())
            .toList();

    BigDecimal pagado = pagosConfirmados.stream()
            .map(PagoParcial::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // ✅ Usamos montoFinal si ya fue calculado y guardado
    BigDecimal montoFinal = cuota.getMontoFinal();

    if (montoFinal == null) {
        montoFinal = cuota.getMonto();

        // ⚠️ Solo calcular intereses si aún no está definido
        if (cuota.estaAtrasada()) {
            long mesesAtrasados = ChronoUnit.MONTHS.between(cuota.getFechaPago(), LocalDate.now());
            if (mesesAtrasados > 0) {
                BigDecimal interesMensual = new BigDecimal("1.25");
                BigDecimal factorInteres = interesMensual.pow((int) mesesAtrasados);
                montoFinal = montoFinal.multiply(factorInteres).setScale(2, RoundingMode.HALF_UP);
            }
        }

        // ✅ Guardar ese monto final en la BD para futuras consultas
        cuota.setMontoFinal(montoFinal);
        cuotaRepository.save(cuota);
    }

    return montoFinal.subtract(pagado).max(BigDecimal.ZERO);
}

        public Optional<Cuota> buscarPorId(Long id) {
            return cuotaRepository.findById(id);
        }
        @Override
public void verificarYGenerarComprobanteFinal(Long cuotaId) {
    Cuota cuota = cuotaRepository.findById(cuotaId)
        .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

    if (cuota.isPagado()) {
        throw new IllegalStateException("La cuota ya fue pagada.");
    }

    List<PagoParcial> pagosConfirmados = pagoParcialRepository.findByCuotaId(cuotaId).stream()
        .filter(p -> p.getEstadoPago() == EstadoPago.CONFIRMADO)
        .toList();

    BigDecimal totalPagado = pagosConfirmados.stream()
        .map(PagoParcial::getMonto)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal montoFinal = cuota.getMontoFinal() != null
        ? cuota.getMontoFinal()
        : cuota.getMonto(); // por si no lo calcularon al crear

    BigDecimal restante = montoFinal.subtract(totalPagado);

    // ✅ Se permite cerrar la cuota si el restante es <= 0.10 soles
    if (restante.compareTo(new BigDecimal("0.10")) > 0) {
        throw new IllegalStateException("Aún falta pagar la cuota completa.");
    }

    // Generar comprobante único
    String numeroComprobante = generarCodigoUnico();
    cuota.setPagado(true);
    cuota.setFechaPagoReal(LocalDate.now());
    cuota.setComprobante(numeroComprobante);
    cuotaRepository.save(cuota);

    // Crear y guardar el PDF
    byte[] pdf = generarPdfConDetalleMedios(pagosConfirmados, cuota, numeroComprobante);

    ComprobantePdf comprobantePdf = new ComprobantePdf();
    comprobantePdf.setNumeroComprobante(numeroComprobante);
    comprobantePdf.setNombreArchivo("comprobante_total_" + cuota.getId() + ".pdf");
    comprobantePdf.setArchivoPdf(pdf);
    comprobantePdf.setCuota(cuota);

    comprobantePdfRepository.save(comprobantePdf);
}

}
