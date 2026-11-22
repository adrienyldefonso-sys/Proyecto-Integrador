package com.ojodchakra.agricultor.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ClaveApiInterceptor implements HandlerInterceptor {

    private final byte[] clave;

    public ClaveApiInterceptor(String clave) {
        this.clave = clave.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String recibida = request.getHeader("X-Api-Key");
        if (recibida != null && MessageDigest.isEqual(clave, recibida.getBytes(StandardCharsets.UTF_8))) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"title\":\"No autorizado\",\"status\":401,"
                        + "\"detail\":\"Falta la clave de la API o es incorrecta.\"}");
        return false;
    }
}