package emer.backend.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import emer.backend.entidades.Cliente;
import emer.backend.entidades.Cuota;
import emer.backend.entidades.Prestamo;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerator {

    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final BigDecimal LATE_FEE_PERCENTAGE = new BigDecimal("0.01"); // 1%

    public byte[] generarComprobante(Cuota cuota) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A6, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document, cuota);
            addClientData(document, cuota.getPrestamo().getCliente());
            addDetailsTable(document, cuota);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    public byte[] generarPdf(Prestamo prestamo) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document, prestamo);
            addClientData(document, prestamo.getCliente());
            addLoanDetails(document, prestamo);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error al generar el PDF del cronograma", e);
        }
    }

    private void addHeader(Document document, Prestamo prestamo) throws DocumentException {
        Paragraph header = new Paragraph("Cronograma de Pagos", BOLD_FONT);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(20f);
        document.add(header);
    }

    private void addLoanDetails(Document document, Prestamo prestamo) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        table.addCell(new PdfPCell(new Phrase("N° Cuota", BOLD_FONT)));
        table.addCell(new PdfPCell(new Phrase("Fecha Vencimiento", BOLD_FONT)));
        table.addCell(new PdfPCell(new Phrase("Monto", BOLD_FONT)));
        table.addCell(new PdfPCell(new Phrase("Estado", BOLD_FONT)));

        if (prestamo.getCuotas() != null) {
            for (Cuota cuota : prestamo.getCuotas()) {
                table.addCell(String.valueOf(cuota.getNumero()));
                table.addCell(cuota.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                table.addCell(cuota.getMonto().toPlainString());
                table.addCell(cuota.isPagado() ? "Pagado" : "Pendiente");
            }
        }

        document.add(table);
    }

    private void addHeader(Document document, Cuota cuota) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(20f);

        PdfPCell logoCell = new PdfPCell(new Phrase("SATT", BOLD_FONT));
        logoCell.setBorder(PdfPCell.NO_BORDER);
        headerTable.addCell(logoCell);

        PdfPCell receiptCell = new PdfPCell(new Phrase("RECIBO N°: " + cuota.getId(), BOLD_FONT));
        receiptCell.setBorder(PdfPCell.NO_BORDER);
        receiptCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerTable.addCell(receiptCell);

        document.add(headerTable);
    }

    private void addClientData(Document document, Cliente cliente) throws DocumentException {
        document.add(new Paragraph("Nombre: " + cliente.getNombre(), NORMAL_FONT));
        document.add(new Paragraph("Domicilio: " + cliente.getDireccion(), NORMAL_FONT));
        document.add(new Paragraph("Periodo: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy")), NORMAL_FONT));
        document.add(new Paragraph("\n"));
    }

    private void addDetailsTable(Document document, Cuota cuota) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        PdfPCell infoHeader = new PdfPCell(new Phrase("INFORMACIÓN", BOLD_FONT));
        infoHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(infoHeader);

        PdfPCell detailsHeader = new PdfPCell(new Phrase("DETALLE DE COBRANZA", BOLD_FONT));
        detailsHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(detailsHeader);

        PdfPCell infoCell = new PdfPCell();
        infoCell.addElement(new Phrase("El interés moratorio de las deudas vencidas del SATT es de 1% mensual", NORMAL_FONT));
        table.addCell(infoCell);

        PdfPCell detailsCell = createDetailsCell(cuota);
        table.addCell(detailsCell);

        document.add(table);
    }

    private PdfPCell createDetailsCell(Cuota cuota) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);

        PdfPTable detailsSubTable = new PdfPTable(2);
        detailsSubTable.setWidthPercentage(100);

        addDetailRow(detailsSubTable, "Monto Cuota:", cuota.getMonto());

        BigDecimal mora = calculateLateFee(cuota);
        if (mora.compareTo(BigDecimal.ZERO) > 0) {
            addDetailRow(detailsSubTable, "Mora por Atraso:", mora);
        }

        BigDecimal total = cuota.getMonto().add(mora);
        addDetailRow(detailsSubTable, "TOTAL:", total);

        cell.addElement(detailsSubTable);
        return cell;
    }

    private void addDetailRow(PdfPTable table, String label, BigDecimal value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase("S/ " + value.toPlainString(), NORMAL_FONT));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(valueCell);
    }

    private BigDecimal calculateLateFee(Cuota cuota) {
        LocalDate fechaVencimiento = cuota.getFechaPago();
        if (LocalDate.now().isAfter(fechaVencimiento) && !cuota.isPagado()) {
            BigDecimal montoCuota = cuota.getMonto();
            return montoCuota.multiply(LATE_FEE_PERCENTAGE);
        }
        return BigDecimal.ZERO;
    }
}
