package emer.backend.service.interfaces;

import org.springframework.http.ResponseEntity;

public interface CronogramaService {
    ResponseEntity<byte[]> descargarPdf(Long prestamoId);
}