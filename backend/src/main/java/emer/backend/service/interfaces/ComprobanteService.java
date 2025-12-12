package emer.backend.service.interfaces;

import org.springframework.http.ResponseEntity;

public interface ComprobanteService {
    ResponseEntity<byte[]> descargarPdf(Long cuotaId);
}
