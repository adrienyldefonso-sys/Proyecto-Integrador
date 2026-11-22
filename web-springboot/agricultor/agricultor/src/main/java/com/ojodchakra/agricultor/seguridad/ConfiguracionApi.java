package com.ojodchakra.agricultor.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionApi implements WebMvcConfigurer {

    private final String clave;

    public ConfiguracionApi(@Value("${app.api-key}") String clave) {
        this.clave = clave;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ClaveApiInterceptor(clave)).addPathPatterns("/api/**");
    }
}