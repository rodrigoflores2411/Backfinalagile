package emer.backend.service.impl;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import emer.backend.entidades.CronogramaPdf;
import emer.backend.entidades.Prestamo;
import emer.backend.repository.CronogramaPdfRepository;
import emer.backend.service.interfaces.CronogramaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CronogramaServiceImpl implements CronogramaService {
    private final CronogramaPdfRepository cronogramaPdfRepository;

    @Override
    public ResponseEntity<byte[]> descargarPdf(Long prestamoId) {
        CronogramaPdf cronograma = cronogramaPdfRepository.findByPrestamo(new Prestamo(prestamoId))
                .orElseThrow(() -> new RuntimeException("PDF no encontrado"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(cronograma.getNombreArchivo()).build());
        return new ResponseEntity<>(cronograma.getArchivoPdf(), headers, HttpStatus.OK);
    }
}