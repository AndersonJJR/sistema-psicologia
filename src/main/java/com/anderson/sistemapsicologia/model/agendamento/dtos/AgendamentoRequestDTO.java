package com.anderson.sistemapsicologia.model.agendamento.dtos;

import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoRequestDTO(
        @NotNull(message = "O psicólogo é obrigatório")
        UUID idPsicologo,
        @NotNull(message = "O paciente é obrigatório")
        UUID idPaciente,
        @NotNull(message = "A data e hora da sessão são obrigatórias")
        LocalDateTime dataHoraSessao,
        @NotNull(message = "A modalidade de atendimento é obrigatória")
        ModalidadeAtendimento modalidade,
        StatusAgendamento status,
        String enderecoLocalSessao,
        Integer duracaoMinutos,
        String tipoSessao,
        Boolean repetirSemanalmente,
        Integer quantidadeSemanas,
        BigDecimal valorHonorario,
        Boolean enviarLembrete24h
) {
    public AgendamentoRequestDTO(
            UUID idPsicologo,
            UUID idPaciente,
            LocalDateTime dataHoraSessao,
            ModalidadeAtendimento modalidade,
            StatusAgendamento status,
            String enderecoLocalSessao
    ) {
        this(idPsicologo, idPaciente, dataHoraSessao, modalidade, status, enderecoLocalSessao, null, null, null, null, null, null);
    }
}
