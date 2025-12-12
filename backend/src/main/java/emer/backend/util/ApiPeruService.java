package emer.backend.util;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import emer.backend.dto.ClienteDTO;

@Service
public class ApiPeruService {

    @Value("${apisperu.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    public ClienteDTO buscarYCrearCliente(String doc) {
        String url;
        HttpHeaders headers = new HttpHeaders();
    
        if (doc.length() == 8) {
            url = "https://api.apis.net.pe/v1/dni?numero=" + doc;
            headers.set("Authorization", "Bearer " + token); // Para DNI
        } else {
            url = "https://dniruc.apisperu.com/api/v1/ruc/" + doc + "?token=" + token;
            // No se necesita Authorization header para este endpoint
        }
    
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response;
    
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        } catch (Exception e) {
            System.out.println("❌ Error al conectar con APIsPeru: " + e.getMessage());
            return null;
        }
    
        if (!response.getStatusCode().is2xxSuccessful()) {
            System.out.println("❌ La API devolvió un estado no exitoso: " + response.getStatusCode());
            return null;
        }
    
        Map data = response.getBody();
        if (data == null) {
            System.out.println("❌ Respuesta vacía desde la API");
            return null;
        }
    
        System.out.println("✅ Datos recibidos: " + data);
    
        ClienteDTO dto = new ClienteDTO();
        dto.setDniRuc(doc);
    
        if (doc.length() == 8) {
            // Persona natural (DNI)
            String nombres = safeString(data.get("nombres"));
            String apellidoPaterno = safeString(data.get("apellidoPaterno"));
            String apellidoMaterno = safeString(data.get("apellidoMaterno"));
    
            String nombreCompleto = (nombres + " " + apellidoPaterno + " " + apellidoMaterno).trim();
            if (nombreCompleto.isBlank()) {
                System.out.println("❌ Nombre completo vacío para DNI.");
                return null;
            }
    
            dto.setNombre(nombreCompleto);
            dto.setDireccion("-");
        } else {
            // Persona jurídica (RUC)
            Object razonRaw = data.get("razonSocial");
            Object direccionRaw = data.get("direccion");
    
            if (razonRaw == null || razonRaw.toString().isBlank()) {
                System.out.println("❌ La API no devolvió razonSocial válida: " + data);
                return null;
            }
    
            dto.setNombre(razonRaw.toString().trim());
            dto.setDireccion(direccionRaw != null && !direccionRaw.toString().isBlank()
                    ? direccionRaw.toString().trim()
                    : "-");
        }
    
        return dto;
    }
    

    private String safeString(Object value) {
        return value != null ? value.toString().trim() : "";
    }
}
