package emer.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PrestamoRequest {
    private String dniRuc;
    private BigDecimal monto;
    private Integer plazoMeses;
    private LocalDate fechaEmision; // ✅ NUEVO
}