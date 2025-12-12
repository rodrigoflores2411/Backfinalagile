package emer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import emer.backend.service.interfaces.ComprobanteService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @GetMapping("/{cuotaId}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable Long cuotaId) {
        return comprobanteService.descargarPdf(cuotaId);
    }
}
