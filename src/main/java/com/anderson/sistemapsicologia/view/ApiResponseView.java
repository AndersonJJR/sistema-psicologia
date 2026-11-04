package com.anderson.sistemapsicologia.view;

import java.time.Instant;

/**
 * View padronizada para respostas da API REST.
 */
public record ApiResponseView<T>(
        boolean sucesso,
        String mensagem,
        T dados,
        Instant timestamp
) {
    public static <T> ApiResponseView<T> ok(String mensagem, T dados) {
        return new ApiResponseView<>(true, mensagem, dados, Instant.now());
    }
}
