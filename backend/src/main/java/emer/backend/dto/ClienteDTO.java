package emer.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import emer.backend.entidades.*;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private String dniRuc;
    private String nombre;
    private String direccion;
    private String apellido; // solo visible para frontend

    public ClienteDTO(Cliente cliente) {
        this.dniRuc = cliente.getDniRuc();
        this.nombre = cliente.getNombre();
        this.direccion = cliente.getDireccion();
        this.apellido = ""; // opcional
    }
}