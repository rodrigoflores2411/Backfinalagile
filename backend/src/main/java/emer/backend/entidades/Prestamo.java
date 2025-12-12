    package emer.backend.entidades;


    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.util.List;
    
    import com.fasterxml.jackson.annotation.JsonIgnore;

    @Entity
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Table(name = "prestamo")
    public class Prestamo {

        public Prestamo(Long prestamoId) {
            this.id = prestamoId;
        }

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "cliente_id")
        private Cliente cliente;

        @Column(nullable = false)
        private LocalDate fecha = LocalDate.now();

        @Column(nullable = false)
        private BigDecimal monto;

        @Column(name = "plazo_meses", nullable = false)
        private Integer plazoMeses;

        @Column(name = "interes_total", nullable = false)
        private BigDecimal interesTotal;

        @OneToMany(mappedBy = "prestamo")
        @JsonIgnore  // <- evita que el préstamo serialice sus cuotas
        private List<Cuota> cuotas;

    }
