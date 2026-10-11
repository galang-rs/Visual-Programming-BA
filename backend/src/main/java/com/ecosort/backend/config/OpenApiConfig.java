package com.ecosort.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("EcoSort RESTful API Documentation")
                        .version("1.0.0")
                        .description(
                                "Dokumentasi resmi API EcoSort - Platform Digital Bank Sampah & Ekonomi Sirkular.\n\n"
                                        +
                                        "### Panduan Autentikasi Pengujian:\n" +
                                        "1. Lakukan login di endpoint `/api/auth/login` (atau gunakan kredensial demo).\n"
                                        +
                                        "2. Salin token dari field `token` pada respons JSON.\n"
                                        +
                                        "3. Klik tombol hijau **Authorize** di pojok kanan atas, lalu paste token ke kolom `Value` (tanpa kata 'Bearer ').\n"
                                        +
                                        "4. Anda dapat mencoba seluruh endpoint yang memerlukan otorisasi (Role `WARGA` atau `PETUGAS`).")
                        .contact(new Contact()
                                .name("Tim Pengembang EcoSort")
                                .email("dev@ecosort.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Masukkan token JWT yang diperoleh dari endpoint /api/auth/login")));
    }
}
