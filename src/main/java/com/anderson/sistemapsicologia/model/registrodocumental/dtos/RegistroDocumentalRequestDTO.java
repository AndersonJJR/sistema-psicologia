package com.anderson.sistemapsicologia.model.registrodocumental.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegistroDocumentalRequestDTO(
        @NotNull(message = "O identificador do agendamento é obrigatório")
        UUID idAgendamento,
        @NotBlank(message = "As hipóteses provisórias são obrigatórias")
        String hipotesesProvisorias,
        @NotBlank(message = "As notas de supervisão são obrigatórias")
        String notasSupervisao
) {
}
