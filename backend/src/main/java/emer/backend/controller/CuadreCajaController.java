package emer.backend.controller;

import emer.backend.service.interfaces.CuadreCajaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/cuadre-caja")
public class CuadreCajaController {

    @Autowired
    private CuadreCajaService cuadreCajaService;

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> exportToExcel(@RequestParam("anio") int anio, @RequestParam("mes") int mes) {
        ByteArrayInputStream in = cuadreCajaService.generarCuadreCajaExcel(anio, mes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=cuadre_caja.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(new InputStreamResource(in));
    }
}
