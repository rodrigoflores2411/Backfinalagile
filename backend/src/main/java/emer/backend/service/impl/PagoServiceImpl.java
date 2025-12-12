package emer.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import java.util.Collections;
import java.util.List;

import emer.backend.entidades.Cuota;
import emer.backend.entidades.PagoParcial;
import emer.backend.repository.CuotaRepository;
import emer.backend.repository.PagoParcialRepository;
import emer.backend.service.interfaces.PagoService;
import java.io.IOException;

@Service
public class PagoServiceImpl implements PagoService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private PagoParcialRepository pagoParcialRepository;

    @Autowired
    private CuotaRepository cuotaRepository;



        @Override
        public PagoParcial registrarPagoParcial(Long cuotaId, String metodo, BigDecimal monto, MultipartFile file) {
            try {
                Cuota cuota = cuotaRepository.findById(cuotaId)
                        .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

                String url = null;
                if (file != null && !file.isEmpty()) {
                    Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), Collections.emptyMap());

                    url = uploadResult.get("secure_url").toString();
                }

                PagoParcial pago = new PagoParcial();
                pago.setCuota(cuota);
                pago.setMetodoPago(metodo);
                pago.setMonto(monto);
                pago.setComprobanteUrl(url);
                pago.setFechaPago(LocalDate.now());

                return pagoParcialRepository.save(pago);

            } catch (IOException e) {
                throw new RuntimeException("Error al subir comprobante a Cloudinary", e);
            }
        }
        @Override
        public List<PagoParcial> obtenerPagosParcialesPorCuota(Long cuotaId) {
            return pagoParcialRepository.findByCuotaId(cuotaId);
        }
}