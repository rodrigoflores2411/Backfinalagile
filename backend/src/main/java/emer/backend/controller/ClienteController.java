package emer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import emer.backend.dto.ClienteDTO;
import emer.backend.service.interfaces.ClienteService;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping("/buscar/{documento}")
    public ResponseEntity<ClienteDTO> buscarCliente(@PathVariable String documento) {
        return ResponseEntity.ok(clienteService.buscarODescargar(documento));
    }
}
