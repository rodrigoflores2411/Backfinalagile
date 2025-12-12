package emer.backend.controller;

import emer.backend.entidades.CronogramaPdf;
import emer.backend.repository.CronogramaPdfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cronograma")
@RequiredArgsConstructor
public class CronogramaController {

    private final CronogramaPdfRepository cronogramaPdfRepository;

    @GetMapping("/descargar/{prestamoId}")
    public ResponseEntity<byte[]> descargar(@PathVariable Long prestamoId) {
        CronogramaPdf cronograma = cronogramaPdfRepository.findByPrestamoId(prestamoId)
                .orElseThrow(() -> new RuntimeException("No se encontró el PDF del préstamo"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(cronograma.getNombreArchivo()).build());

        return new ResponseEntity<byte[]>(cronograma.getArchivoPdf(), headers, HttpStatus.OK);

    }
}
