package com.anderson.sistemapsicologia.model.prontuario.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProntuarioClinicoRequestDTO(
        @NotNull(message = "O identificador do agendamento é obrigatório")
        Long idAgendamento,
        @NotBlank(message = "A descrição dos procedimentos técnicos é obrigatória")
        String procedimentosTecnicos,
        @NotBlank(message = "A evolução observável é obrigatória")
        String evolucaoObservavel
) {
}
