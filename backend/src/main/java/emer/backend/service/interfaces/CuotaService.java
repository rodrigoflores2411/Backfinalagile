package emer.backend.service.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import emer.backend.dto.DetallePagoDTO;
import emer.backend.entidades.Cuota;

public interface CuotaService {
    public List<Cuota> obtenerDeudasCliente(Long clienteId);
    public List<Cuota> obtenerCuotasPendientes();
    public BigDecimal obtenerRestanteCuota(Long cuotaId);
    public Optional<Cuota> buscarPorId(Long id);
    public void verificarYGenerarComprobanteFinal(Long cuotaId);
    public void pagarCuota(Long cuotaId, List<DetallePagoDTO> pagos);
    }
