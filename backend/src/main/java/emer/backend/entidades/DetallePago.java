package emer.backend.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

@Entity
public class DetallePago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Cuota cuota;

    private String medioPago; // EFECTIVO, BILLETERA, MERCADOPAGO
    private BigDecimal monto;
    private String comprobanteUrl; // solo si aplica
    private LocalDate fechaPago;
}