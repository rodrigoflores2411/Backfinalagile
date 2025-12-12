package emer.backend.entidades;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "cuota")
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prestamo_id")
    @JsonIgnoreProperties("cuotas")  // <- evita el ciclo
    private Prestamo prestamo;


    @Column(nullable = false)
    private Integer numero;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private boolean pagado = false;

    private LocalDate fechaPagoReal;

    private String comprobante; // Puede ser nombre de archivo o código

    @Enumerated(EnumType.STRING)
    private MedioPago medioPago;
    public enum MedioPago {
    EFECTIVO,
    TRANSFERENCIA,
    BILLETERA_DIGITAL,
}
public boolean estaAtrasada() {
    return !this.pagado && this.fechaPago.isBefore(LocalDate.now());
}

public boolean esDeEsteMes() {
    LocalDate ahora = LocalDate.now();
    return this.fechaPago.getMonth() == ahora.getMonth() && this.fechaPago.getYear() == ahora.getYear();
}

    @Column(precision = 10, scale = 2)
    private BigDecimal intereses;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoFinal;

}
