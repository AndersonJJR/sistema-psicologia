package com.anderson.sistemapsicologia.model.agendamento.dtos;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;

import java.time.Instant;
import java.time.LocalDateTime;

public record AgendamentoResponseDTO(
        Long idAgendamento,
        Long idPsicologo,
        String nomePsicologo,
        String numeroEpsiPsicologo,
        Long idPaciente,
        String nomePaciente,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        LocalDateTime dataHoraSessao,
        ModalidadeAtendimento modalidade,
        StatusAgendamento status,
        String enderecoLocalSessao,
        boolean ativo,
        Instant deletedAt
) {
    public static AgendamentoResponseDTO fromEntity(Agendamento entity) {
        return new AgendamentoResponseDTO(
                entity.getIdAgendamento(),
                entity.getPsicologo().getIdPsicologo(),
                entity.getPsicologo().getNome(),
                entity.getPsicologo().getNumeroEpsi(),
                entity.getPaciente().getIdPaciente(),
                entity.getPaciente().getNome(),
                entity.getPaciente().getContatoEmergenciaNome(),
                entity.getPaciente().getContatoEmergenciaTelefone(),
                entity.getDataHoraSessao(),
                entity.getModalidade(),
                entity.getStatus(),
                entity.getEnderecoLocalSessao(),
                entity.isAtivo(),
                entity.getDeletedAt()
        );
    }
}
