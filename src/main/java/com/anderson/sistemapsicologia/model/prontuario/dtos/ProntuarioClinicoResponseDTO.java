package com.anderson.sistemapsicologia.model.prontuario.dtos;

import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinico;

import java.time.Instant;
import java.util.UUID;

public record ProntuarioClinicoResponseDTO(
        UUID idProntuario,
        UUID idAgendamento,
        UUID idPaciente,
        String nomePaciente,
        UUID idPsicologo,
        String nomePsicologo,
        String procedimentosTecnicos,
        String evolucaoObservavel,
        Instant dataRegistro,
        boolean assinado,
        String hashAssinatura,
        boolean ativo,
        Instant deletedAt
) {
    public static ProntuarioClinicoResponseDTO fromEntity(ProntuarioClinico entity) {
        return new ProntuarioClinicoResponseDTO(
                entity.getIdProntuario(),
                entity.getAgendamento().getIdAgendamento(),
                entity.getAgendamento().getPaciente().getIdPaciente(),
                entity.getAgendamento().getPaciente().getNome(),
                entity.getAgendamento().getPsicologo().getIdPsicologo(),
                entity.getAgendamento().getPsicologo().getNome(),
                entity.getProcedimentosTecnicos(),
                entity.getEvolucaoObservavel(),
                entity.getDataRegistro(),
                entity.isAssinado(),
                entity.getHashAssinatura(),
                entity.isAtivo(),
                entity.getDeletedAt()
        );
    }
}
