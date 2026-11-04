package com.anderson.sistemapsicologia.model.agendamento.dtos;

import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoRequestDTO(
        @NotNull(message = "O psicólogo é obrigatório")
        Long idPsicologo,
        @NotNull(message = "O paciente é obrigatório")
        Long idPaciente,
        @NotNull(message = "A data e hora da sessão são obrigatórias")
        LocalDateTime dataHoraSessao,
        @NotNull(message = "A modalidade de atendimento é obrigatória")
        ModalidadeAtendimento modalidade,
        StatusAgendamento status,
        String enderecoLocalSessao
) {
}
