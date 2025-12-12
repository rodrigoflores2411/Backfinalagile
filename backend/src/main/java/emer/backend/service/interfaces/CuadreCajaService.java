package emer.backend.service.interfaces;

import java.io.ByteArrayInputStream;

public interface CuadreCajaService {
    ByteArrayInputStream generarCuadreCajaExcel(int anio, int mes);
}
