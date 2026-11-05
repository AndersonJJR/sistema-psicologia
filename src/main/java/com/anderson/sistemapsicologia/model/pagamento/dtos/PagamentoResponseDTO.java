package com.anderson.sistemapsicologia.model.pagamento.dtos;

import com.anderson.sistemapsicologia.model.pagamento.Pagamento;
import com.anderson.sistemapsicologia.model.pagamento.StatusSplit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagamentoResponseDTO(
        UUID idPagamento,
        UUID idAgendamento,
        UUID idPaciente,
        String nomePaciente,
        String cpfPaciente,
        String modalidadeSessao,
        UUID idPsicologo,
        String nomePsicologo,
        BigDecimal valorTotal,
        BigDecimal taxaClinicaInfraestrutura,
        BigDecimal repasseProfissional,
        StatusSplit statusSplit,
        String meioPagamento,
        Instant dataPagamento,
        boolean ativo
) {
    public static PagamentoResponseDTO fromEntity(Pagamento entity) {
        return new PagamentoResponseDTO(
                entity.getIdPagamento(),
                entity.getAgendamento().getIdAgendamento(),
                entity.getAgendamento().getPaciente().getIdPaciente(),
                entity.getAgendamento().getPaciente().getNome(),
                entity.getAgendamento().getPaciente().getCpf(),
                entity.getAgendamento().getModalidade().name(),
                entity.getAgendamento().getPsicologo().getIdPsicologo(),
                entity.getAgendamento().getPsicologo().getNome(),
                entity.getValorTotal(),
                entity.getTaxaClinicaInfraestrutura(),
                entity.getRepasseProfissional(),
                entity.getStatusSplit(),
                entity.getMeioPagamento() != null ? entity.getMeioPagamento() : "PIX",
                entity.getDataPagamento(),
                entity.isAtivo()
        );
    }
}
