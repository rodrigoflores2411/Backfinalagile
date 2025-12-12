package emer.backend.dto;

import emer.backend.entidades.Prestamo;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PrestamoDTO {
    private Long id;
    private String dniRuc;
    private String nombre;
    private LocalDate fecha;
    private BigDecimal monto;

    public PrestamoDTO(Prestamo p) {
        this.id = p.getId();
        this.dniRuc = p.getCliente().getDniRuc();
        this.nombre = p.getCliente().getNombre();
        this.fecha = p.getFecha();
        this.monto = p.getMonto();
    }
}
