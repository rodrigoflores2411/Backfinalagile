package emer.backend.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

@Entity
public class PagoParcial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Cuota cuota;

    private String metodoPago;

    private BigDecimal monto;

    private String comprobanteUrl;

    private LocalDate fechaPago;

    @Column(name = "numero_comprobante")
    private String numeroComprobante;

    @Column(name = "nombre_archivo_pdf")
    private String nombreArchivoPdf;
    @Column(name = "estado_pago")
    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.CONFIRMADO;
    public enum EstadoPago {
    PENDIENTE,
    CONFIRMADO
}


}
