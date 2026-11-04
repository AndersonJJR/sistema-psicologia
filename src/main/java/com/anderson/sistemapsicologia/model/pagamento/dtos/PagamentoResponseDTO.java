package com.anderson.sistemapsicologia.model.pagamento.dtos;

import com.anderson.sistemapsicologia.model.pagamento.Pagamento;
import com.anderson.sistemapsicologia.model.pagamento.StatusSplit;

import java.math.BigDecimal;
import java.time.Instant;

public record PagamentoResponseDTO(
        Long idPagamento,
        Long idAgendamento,
        Long idPaciente,
        String nomePaciente,
        Long idPsicologo,
        String nomePsicologo,
        BigDecimal valorTotal,
        BigDecimal taxaClinicaInfraestrutura,
        BigDecimal repasseProfissional,
        StatusSplit statusSplit,
        Instant dataPagamento,
        boolean ativo
) {
    public static PagamentoResponseDTO fromEntity(Pagamento entity) {
        return new PagamentoResponseDTO(
                entity.getIdPagamento(),
                entity.getAgendamento().getIdAgendamento(),
                entity.getAgendamento().getPaciente().getIdPaciente(),
                entity.getAgendamento().getPaciente().getNome(),
                entity.getAgendamento().getPsicologo().getIdPsicologo(),
                entity.getAgendamento().getPsicologo().getNome(),
                entity.getValorTotal(),
                entity.getTaxaClinicaInfraestrutura(),
                entity.getRepasseProfissional(),
                entity.getStatusSplit(),
                entity.getDataPagamento(),
                entity.isAtivo()
        );
    }
}
