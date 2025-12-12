        package emer.backend.service.impl;

        import java.math.BigDecimal;
        import java.math.RoundingMode;
        import java.time.LocalDate;
        import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

        import org.springframework.http.HttpStatus;
        import org.springframework.stereotype.Service;
        import org.springframework.web.server.ResponseStatusException;

        import emer.backend.dto.PrestamoRequest;
        import emer.backend.entidades.Cliente;
        import emer.backend.entidades.CronogramaPdf;
        import emer.backend.entidades.Cuota;
        import emer.backend.entidades.Prestamo;
        import emer.backend.repository.ClienteRepository;
        import emer.backend.repository.CronogramaPdfRepository;
        import emer.backend.repository.CuotaRepository;
        import emer.backend.repository.PrestamoRepository;
        import emer.backend.service.interfaces.PrestamoService;
        import emer.backend.util.PdfGenerator;
        import lombok.RequiredArgsConstructor;
        import emer.backend.util.FechaDesdeAPI;

        @Service
        @RequiredArgsConstructor
        public class PrestamoServiceImpl implements PrestamoService {

        private final PrestamoRepository prestamoRepository;
        private final ClienteRepository clienteRepository;
        private final CuotaRepository cuotaRepository;
        private final CronogramaPdfRepository cronogramaPdfRepository;
        private final PdfGenerator pdfGenerator;

        private static final BigDecimal LIMITE_DIARIO = new BigDecimal("19000");
        private static final BigDecimal LIMITE_MENSUAL = new BigDecimal("76000");

        @Override
        public void crearPrestamo(PrestamoRequest request) {
                Cliente cliente = clienteRepository.findByDniRuc(request.getDniRuc())
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

             
                LocalDate hoy = request.getFechaEmision();
   

                BigDecimal montoNuevo = request.getMonto();

                // Validación diaria
                List<Prestamo> prestamosHoy = prestamoRepository.findByClienteAndFecha(cliente, hoy);
                BigDecimal totalDia = prestamosHoy.stream()
                        .map(Prestamo::getMonto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalDia.add(montoNuevo).compareTo(LIMITE_DIARIO) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Límite diario de préstamos superado (S/19,000)");
                }

                // Validación mensual
                int mes = hoy.getMonthValue();
                int anio = hoy.getYear();
                List<Prestamo> prestamosMes = prestamoRepository.findByClienteInMes(cliente, mes, anio);
                BigDecimal totalMes = prestamosMes.stream()
                        .map(Prestamo::getMonto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalMes.add(montoNuevo).compareTo(LIMITE_MENSUAL) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Límite mensual de préstamos superado (S/76,000)");
                }

                // Registro del préstamo
                BigDecimal interesTotal = montoNuevo.multiply(BigDecimal.valueOf(1.10));
                Prestamo prestamo = new Prestamo();
                prestamo.setCliente(cliente);
                prestamo.setMonto(montoNuevo);
                prestamo.setPlazoMeses(request.getPlazoMeses());
                prestamo.setInteresTotal(interesTotal);
                prestamo.setFecha(hoy);
                prestamo = prestamoRepository.save(prestamo);

                // Registro de cuotas
                BigDecimal cuotaMensual = interesTotal
                        .divide(BigDecimal.valueOf(request.getPlazoMeses()), 2, RoundingMode.HALF_UP);

                LocalDate hoy2 = FechaDesdeAPI.obtenerFechaDesdeAPI();  // Fecha real del sistema para validar mora

                for (int i = 1; i <= request.getPlazoMeses(); i++) {
                Cuota cuota = new Cuota();
                cuota.setPrestamo(prestamo);
                cuota.setNumero(i);

                LocalDate fechaPago = hoy.plusMonths(i);
                cuota.setFechaPago(fechaPago);
                cuota.setMonto(cuotaMensual);

                BigDecimal intereses = BigDecimal.ZERO;
                BigDecimal montoFinal = cuotaMensual;

                if (fechaPago.isEqual(hoy2)) {
                        // Vence hoy: sin interés
                        montoFinal = cuotaMensual;
                        intereses = BigDecimal.ZERO;
                } else if (fechaPago.isBefore(hoy2)) {
                        // Cuota vencida: aplicar interés por cada mes vencido completo o parcial
                        long mesesAtrasados = ChronoUnit.MONTHS.between(fechaPago, hoy2);
                        if (fechaPago.plusMonths(mesesAtrasados).isBefore(hoy2)) {
                        mesesAtrasados++; // considerar el mes actual si ya venció parcialmente
                        }

                        BigDecimal interesMensual = new BigDecimal("1.25"); // 25% mensual
                        BigDecimal montoConInteres = cuotaMensual.multiply(interesMensual.pow((int) mesesAtrasados));

                        montoFinal = montoConInteres.setScale(2, RoundingMode.HALF_UP);
                        intereses = montoFinal.subtract(cuotaMensual).setScale(2, RoundingMode.HALF_UP);
                }

                cuota.setIntereses(intereses);
                cuota.setMontoFinal(montoFinal);

                cuotaRepository.save(cuota);
                }




                // Generación de PDF del cronograma
                byte[] pdfBytes = pdfGenerator.generarPdf(prestamo);
                CronogramaPdf cronograma = new CronogramaPdf(
                        null,
                        prestamo,
                        pdfBytes,
                        "cronograma_prestamo_" + prestamo.getId() + ".pdf",
                        LocalDateTime.now()
                );

                cronogramaPdfRepository.save(cronograma);
        }

        @Override
        public List<Prestamo> obtenerPrestamosPorCliente(String dniRuc) {
                Cliente cliente = clienteRepository.findByDniRuc(dniRuc)
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
                return prestamoRepository.findAllByClienteOrderByFechaDesc(cliente);
        }

        @Override
        public List<Prestamo> obtenerTodosLosPrestamos() {
                return prestamoRepository.findAllByOrderByFechaDesc();
        }

        @Override
        public BigDecimal obtenerTotalMensual(String dniRuc) {
                Cliente cliente = clienteRepository.findByDniRuc(dniRuc)
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

                int mes = LocalDate.now().getMonthValue();
                int anio = LocalDate.now().getYear();

                List<Prestamo> prestamosMes = prestamoRepository.findByClienteInMes(cliente, mes, anio);
                return prestamosMes.stream()
                        .map(Prestamo::getMonto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        }
