
package emer.backend.repository;
import emer.backend.entidades.ComprobantePdf;
import emer.backend.entidades.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;




@Repository
public interface ComprobantePdfRepository extends JpaRepository<ComprobantePdf, Long> {
    Optional<ComprobantePdf> findByNumeroComprobante(String numeroComprobante);
    Optional<ComprobantePdf> findByCuota(Cuota cuota);
    boolean existsByNumeroComprobante(String numeroComprobante);
    Optional<ComprobantePdf> findByCuotaId(Long cuotaId);
}
