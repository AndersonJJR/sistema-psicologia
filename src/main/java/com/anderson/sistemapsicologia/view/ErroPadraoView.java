package com.anderson.sistemapsicologia.view;

import java.time.Instant;
import java.util.List;

/**
 * View padronizada para exibição de erros de domínio, validação e segurança (RBAC/CFP).
 */
public record ErroPadraoView(
        int status,
        String erro,
        List<String> detalhes,
        String caminho,
        Instant timestamp
) {
    public static ErroPadraoView criar(int status, String erro, List<String> detalhes, String caminho) {
        return new ErroPadraoView(status, erro, detalhes, caminho, Instant.now());
    }
}
