package com.anderson.sistemapsicologia.model.auditoria.dtos;

import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogAuditoriaCreateDTO(
        @NotBlank(message = "O identificador do operador é obrigatório")
        String idUsuarioOperador,
        @NotBlank(message = "O endereço IP é obrigatório")
        String enderecoIp,
        @NotNull(message = "A ação de auditoria é obrigatória")
        AcaoAuditoria acao,
        @NotBlank(message = "O recurso acessado é obrigatório")
        String idRecursoAcessado
) {
}
