package com.iotml.wdd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the React dev server (Vite: 5173, CRA: 3000 -- both listed since
 * either could be in use) to call the API, including the SSE stream.
 * EventSource sends a plain cross-origin GET, so this is a standard CORS
 * case -- no special SSE handling needed beyond allowing the origin.
 *
 * Dev-focused as written (hardcoded localhost origins). If this ever needs
 * to serve a deployed frontend too, move the allowed origins into
 * application.properties instead of hardcoding them here.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}