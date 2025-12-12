package emer.backend.service.interfaces;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import emer.backend.entidades.PagoParcial;

public interface PagoService {
    public PagoParcial registrarPagoParcial(Long cuotaId, String metodo, BigDecimal monto, MultipartFile file);
    List<PagoParcial> obtenerPagosParcialesPorCuota(Long cuotaId);

}
