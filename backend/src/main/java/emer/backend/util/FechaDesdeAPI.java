package emer.backend.util;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;

public class FechaDesdeAPI {

    public static LocalDate obtenerFechaDesdeAPI() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://worldtimeapi.org/api/timezone/America/Lima"))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            String fechaHora = json.get("datetime").asText();  // ej: 2025-07-03T15:32:00.000-05:00
            return LocalDate.parse(fechaHora.substring(0, 10)); // extrae solo la fecha

        } catch (Exception e) {
            System.err.println("Error al obtener fecha desde API: " + e.getMessage());
            return LocalDate.now(); // fallback si falla
        }
    }
}
