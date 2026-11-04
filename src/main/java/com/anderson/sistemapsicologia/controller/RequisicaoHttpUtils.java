package com.anderson.sistemapsicologia.controller;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilitário centralizado (DRY) para extração de endereço IP em requisições HTTP.
 */
public final class RequisicaoHttpUtils {

    private RequisicaoHttpUtils() {
    }

    public static String extrairIpCliente(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
