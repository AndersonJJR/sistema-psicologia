package com.anderson.sistemapsicologia.model.auditoria.dtos;

import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.LogAuditoria;

import java.time.Instant;

public record LogAuditoriaResponseDTO(
        Long idLog,
        Instant carimboTempoUtc,
        String idUsuarioOperador,
        String enderecoIp,
        AcaoAuditoria acao,
        String idRecursoAcessado
) {
    public static LogAuditoriaResponseDTO fromEntity(LogAuditoria entity) {
        return new LogAuditoriaResponseDTO(
                entity.getIdLog(),
                entity.getCarimboTempoUtc(),
                entity.getIdUsuarioOperador(),
                entity.getEnderecoIp(),
                entity.getAcao(),
                entity.getIdRecursoAcessado()
        );
    }
}
