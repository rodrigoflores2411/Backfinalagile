package emer.backend.service.impl;

import emer.backend.util.PdfGenerator;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders; // <- CORRECTO
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import emer.backend.entidades.ComprobantePdf;
import emer.backend.entidades.Cuota;
import emer.backend.repository.ComprobantePdfRepository;
import emer.backend.repository.CuotaRepository;
import emer.backend.service.interfaces.ComprobanteService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteServiceImpl implements ComprobanteService {

    private final ComprobantePdfRepository comprobantePdfRepository;
    private final CuotaRepository cuotaRepository;
    private final PdfGenerator pdfGenerator; // Inyecta el nuevo generador

    @Override
    public ResponseEntity<byte[]> descargarPdf(Long cuotaId) {
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        // Busca si ya existe un comprobante
        ComprobantePdf comprobante = comprobantePdfRepository.findByCuota(cuota)
                .orElseGet(() -> {
                    // Si no existe, genera el PDF
                    byte[] pdfBytes = pdfGenerator.generarComprobante(cuota);
                    
                    // Crea y guarda el nuevo comprobante
                    ComprobantePdf nuevoComprobante = new ComprobantePdf();
                    nuevoComprobante.setCuota(cuota);
                    nuevoComprobante.setArchivoPdf(pdfBytes);
                    nuevoComprobante.setNombreArchivo("comprobante_" + cuota.getId() + ".pdf");
                    
                    return comprobantePdfRepository.save(nuevoComprobante);
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Asegura que el nombre del archivo sea único
        headers.setContentDisposition(ContentDisposition.inline().filename(comprobante.getNombreArchivo()).build());

        return new ResponseEntity<>(comprobante.getArchivoPdf(), headers, HttpStatus.OK);
    }
}
