package com.anderson.sistemapsicologia.model.registrodocumental.dtos;

import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumental;

import java.time.Instant;
import java.util.UUID;

public record RegistroDocumentalResponseDTO(
        UUID idRegistroDocumental,
        UUID idAgendamento,
        UUID idPsicologoTitular,
        UUID idPaciente,
        String hipotesesProvisorias,
        String notasSupervisao,
        Instant dataRegistro,
        boolean assinado,
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
                entity.isAssinado(),
                entity.isAtivo(),
                entity.getDeletedAt()
        );
    }
}
