package com.ecosort.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry Point Utama: EcoSortApplication
 * 
 * Menginisialisasi dotenv (.env) untuk memuat konfigurasi port, database, dan JWT secret,
 * lalu meluncurkan container Spring Boot.
 */
@SpringBootApplication
public class EcoSortApplication {

    public static void main(String[] args) {
        // [DOTENV INITIALIZATION]
        // Memuat variabel konfigurasi dari berkas .env bila tersedia
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(EcoSortApplication.class, args);
    }
}
