package com.anderson.sistemapsicologia.model.prontuario.dtos;

import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinico;

import java.time.Instant;

public record ProntuarioClinicoResponseDTO(
        Long idProntuario,
        Long idAgendamento,
        Long idPaciente,
        String nomePaciente,
        Long idPsicologo,
        String nomePsicologo,
        String procedimentosTecnicos,
        String evolucaoObservavel,
        Instant dataRegistro,
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
                entity.isAtivo(),
                entity.getDeletedAt()
        );
    }
}
