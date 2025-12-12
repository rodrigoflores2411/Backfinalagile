package emer.backend.entidades;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "cronograma_pdf")
public class CronogramaPdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "prestamo_id", unique = true)
    private Prestamo prestamo;

    @Lob
    @Column(name = "archivo_pdf", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] archivoPdf;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "fecha_subida", columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaSubida = LocalDateTime.now();
}
