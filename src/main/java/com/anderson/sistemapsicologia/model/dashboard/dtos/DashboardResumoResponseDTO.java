package com.anderson.sistemapsicologia.model.dashboard.dtos;

import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import com.anderson.sistemapsicologia.model.pagamento.StatusSplit;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DashboardResumoResponseDTO(
        BigDecimal repasseLiquidoMes,
        BigDecimal valorBrutoMes,
        BigDecimal retencaoClinicaMes,
        double variacaoPercentualMes,
        List<BigDecimal> sparklineFinanceiro,
        long totalConsultasHoje,
        long totalTelessaudeHoje,
        long totalPresenciaisHoje,
        ProximaConsultaResumoDTO proximaConsulta,
        double taxaAssiduidade,
        long pacientesAtivos,
        long prontuariosEmRascunho,
        List<AgendamentoHojeItemDTO> agendamentosHoje,
        List<AtendimentoRecenteDTO> atendimentosRecentes
) {
    public record ProximaConsultaResumoDTO(
            UUID idAgendamento,
            String horarioInicio,
            long minutosParaInicio
    ) {
    }

    public record AgendamentoHojeItemDTO(
            UUID idAgendamento,
            UUID idPaciente,
            String nomePaciente,
            String avatarUrl,
            int idadePaciente,
            String abordagemClinica,
            long numeroSessao,
            LocalDateTime dataHoraSessao,
            int duracaoMinutos,
            ModalidadeAtendimento modalidade,
            StatusAgendamento status,
            String enderecoLocalSessao,
            String tipoSessao
    ) {
    }

    public record AtendimentoRecenteDTO(
            UUID idAgendamento,
            UUID idPaciente,
            String nomePaciente,
            ModalidadeAtendimento modalidade,
            LocalDateTime dataHoraSessao,
            String prontuarioStatus,
            Instant horaRascunho,
            BigDecimal valorTotal,
            String meioPagamento,
            StatusSplit statusSplit
    ) {
    }
}
