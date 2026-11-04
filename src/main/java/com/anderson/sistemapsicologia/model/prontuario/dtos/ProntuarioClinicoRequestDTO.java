package com.anderson.sistemapsicologia.model.prontuario.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProntuarioClinicoRequestDTO(
        @NotNull(message = "O identificador do agendamento é obrigatório")
        UUID idAgendamento,
        @NotBlank(message = "A descrição dos procedimentos técnicos é obrigatória")
        String procedimentosTecnicos,
        @NotBlank(message = "A evolução observável é obrigatória")
        String evolucaoObservavel
) {
}
