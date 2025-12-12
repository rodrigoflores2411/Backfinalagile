package emer.backend.service.impl;

import org.springframework.stereotype.Service;

import emer.backend.dto.ClienteDTO;
import emer.backend.entidades.Cliente;
import emer.backend.entidades.TipoPersona;
import emer.backend.repository.ClienteRepository;
import emer.backend.service.interfaces.ClienteService;
import emer.backend.util.ApiPeruService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ApiPeruService apiPeruService;

    @Override
    public ClienteDTO buscarODescargar(String documento) {
        try {
            return clienteRepository.findByDniRuc(documento)
                    .map(ClienteDTO::new)
                    .orElseGet(() -> {
                        ClienteDTO nuevo = apiPeruService.buscarYCrearCliente(documento);
                        if (nuevo == null) throw new RuntimeException("Cliente no válido o sin datos suficientes");

                        // Guardar cliente en base de datos
                        Cliente cliente = new Cliente();
                        cliente.setDniRuc(nuevo.getDniRuc());
                        cliente.setNombre(nuevo.getNombre());
                        cliente.setDireccion(nuevo.getDireccion());
                        cliente.setTipoPersona(documento.length() == 8 ? TipoPersona.NATURAL : TipoPersona.JURIDICA);

                        clienteRepository.save(cliente);
                        return nuevo;
                    });
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar o descargar el cliente", e);
        }
    }
}
