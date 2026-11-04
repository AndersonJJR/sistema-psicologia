package com.anderson.sistemapsicologia.model.agendamento.dtos;

import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

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
        String enderecoLocalSessao
) {
}
