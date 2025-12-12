package emer.backend.entidades;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
@Column(name = "tipo_persona", nullable = false)
private TipoPersona tipoPersona;


    @Column(name = "dni_ruc", nullable = false, unique = true)
    private String dniRuc;

    @Column(nullable = false)
    private String nombre;

    private String direccion;
}
