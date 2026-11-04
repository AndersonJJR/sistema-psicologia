package com.anderson.sistemapsicologia.model.paciente.dtos;

import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoResponseDTO;
import com.anderson.sistemapsicologia.model.pagamento.dtos.PagamentoResponseDTO;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoResponseDTO;

import java.time.Instant;
import java.util.List;

/**
 * Record DTO de portabilidade em lote (LGPD e Resolução CFP), consolidando dados estruturados (JSON),
 * representação aberta em CSV e documento binário em padrão PDF/A-1b (codificado em Base64).
 */
public record PacienteExportacaoDTO(
        Instant dataExportacaoUtc,
        String padraoConformidadeDocumental,
        PacienteResponseDTO dadosDemograficos,
        List<AgendamentoResponseDTO> historicoAgendamentos,
        List<ProntuarioClinicoResponseDTO> prontuariosClinicos,
        List<PagamentoResponseDTO> historicoFinanceiroSplit,
        String conteudoCsvAberto,
        String nomeArquivoPdfA,
        String documentoPdfABase64
) {
}
