package emer.backend.service.impl;

import emer.backend.entidades.Cuota;
import emer.backend.repository.CuotaRepository;
import emer.backend.service.interfaces.CuadreCajaService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class CuadreCajaServiceImpl implements CuadreCajaService {

    @Autowired
    private CuotaRepository cuotaRepository;

    @Override
    public ByteArrayInputStream generarCuadreCajaExcel(int anio, int mes) {
        LocalDate startDate = LocalDate.of(anio, mes, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<Cuota> cuotasPagadas = cuotaRepository.findCuotasPagadasEnRangoDeFechas(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Cuadre de Caja");

            // Encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mes de Cuadre", "Cliente", "Nro. Cuota", "Monto Cancelado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Datos
            int rowNum = 1;
            BigDecimal totalMes = BigDecimal.ZERO;
            for (Cuota cuota : cuotasPagadas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(String.format("%d-%02d", anio, mes));

                String nombreCliente = "Dato no disponible";
                if (cuota.getPrestamo() != null && cuota.getPrestamo().getCliente() != null) {
                    nombreCliente = cuota.getPrestamo().getCliente().getNombre();
                }
                row.createCell(1).setCellValue(nombreCliente);

                // SOLUCIÓN FINALÍSIMA: Comprobar si el número de cuota es nulo
                if (cuota.getNumero() != null) {
                    row.createCell(2).setCellValue(cuota.getNumero());
                } else {
                    row.createCell(2).setCellValue(0);
                }

                if (cuota.getMonto() != null) {
                    row.createCell(3).setCellValue(cuota.getMonto().doubleValue());
                    totalMes = totalMes.add(cuota.getMonto());
                } else {
                    row.createCell(3).setCellValue(0.0);
                }
            }

            // Total
            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(2).setCellValue("Total:");
            totalRow.createCell(3).setCellValue(totalMes.doubleValue());

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }
}
