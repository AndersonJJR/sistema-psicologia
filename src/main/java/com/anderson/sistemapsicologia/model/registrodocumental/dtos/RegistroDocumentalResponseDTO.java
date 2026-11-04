package com.anderson.sistemapsicologia.model.registrodocumental.dtos;

import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumental;

import java.time.Instant;

public record RegistroDocumentalResponseDTO(
        Long idRegistroDocumental,
        Long idAgendamento,
        Long idPsicologoTitular,
        Long idPaciente,
        String hipotesesProvisorias,
        String notasSupervisao,
        Instant dataRegistro,
        boolean ativo,
        Instant deletedAt
) {
    public static RegistroDocumentalResponseDTO fromEntity(RegistroDocumental entity) {
        return new RegistroDocumentalResponseDTO(
                entity.getIdRegistroDocumental(),
                entity.getAgendamento().getIdAgendamento(),
                entity.getAgendamento().getPsicologo().getIdPsicologo(),
                entity.getAgendamento().getPaciente().getIdPaciente(),
                entity.getHipotesesProvisorias(),
                entity.getNotasSupervisao(),
                entity.getDataRegistro(),
                entity.isAtivo(),
                entity.getDeletedAt()
        );
    }
}
