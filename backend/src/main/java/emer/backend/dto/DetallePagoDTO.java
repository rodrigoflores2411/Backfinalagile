package emer.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DetallePagoDTO {
    private String medioPago; // EFECTIVO, BILLETERA, MERCADOPAGO
    private BigDecimal monto;
    private String comprobanteUrl; // puede ser null
}