package com.anderson.sistemapsicologia.model.pagamento.dtos;

import com.anderson.sistemapsicologia.model.pagamento.StatusSplit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PagamentoRequestDTO(
        @NotNull(message = "O agendamento é obrigatório")
        UUID idAgendamento,
        @NotNull(message = "O valor_total é obrigatório")
        @Positive(message = "O valor_total deve ser positivo")
        BigDecimal valorTotal,
        @NotNull(message = "A taxa_clinica_infraestrutura é obrigatória")
        BigDecimal taxaClinicaInfraestrutura,
        BigDecimal repasseProfissional,
        StatusSplit statusSplit
) {
}
