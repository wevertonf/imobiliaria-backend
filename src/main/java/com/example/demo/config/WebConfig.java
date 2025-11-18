// src/main/java/com/example/demo/config/WebConfig.java
package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos os endpoints
                .allowedOriginPatterns("http://localhost:3000") // Permitir frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // <--- Permitir credenciais (cookies)
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir arquivos de upload do diretório configurado
        // Ex: app.upload.dir=/home/weverton/Documents/imobiliaria/uploads
        // Então, http://localhost:8080/uploads/... serve arquivos desse diretório
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/home/weverton/Documents/imobiliaria/uploads/");
    }
}