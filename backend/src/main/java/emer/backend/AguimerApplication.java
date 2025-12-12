package emer.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "emer.backend") // Asegura el escaneo de todos tus beans
public class AguimerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AguimerApplication.class, args);
    }
}