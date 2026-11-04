package com.anderson.sistemapsicologia.model.paciente;

import com.anderson.sistemapsicologia.model.agendamento.AgendamentoRepository;
import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoResponseDTO;
import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteExportacaoDTO;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteResponseDTO;
import com.anderson.sistemapsicologia.model.pagamento.PagamentoRepository;
import com.anderson.sistemapsicologia.model.pagamento.dtos.PagamentoResponseDTO;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinicoRepository;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import com.anderson.sistemapsicologia.view.ExportacaoPacienteCsvView;
import com.anderson.sistemapsicologia.view.ExportacaoPacientePdfAView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Serviço responsável pela Portabilidade em Lote (LGPD Art. 18, V e Lei nº 13.787/2018),
 * consolidando dados estruturados (JSON/CSV) e documentos em padrão PDF/A-1b.
 */
@Service
@RequiredArgsConstructor
public class ExportacaoPortabilidadeService {

    private final PacienteService pacienteService;
    private final AgendamentoRepository agendamentoRepository;
    private final ProntuarioClinicoRepository prontuarioRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ExportacaoPacienteCsvView csvView;
    private final ExportacaoPacientePdfAView pdfAView;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public PacienteExportacaoDTO exportarDossiePaciente(Long idPaciente, String enderecoIp) {
        Paciente paciente = pacienteService.buscarEntidadePorId(idPaciente);
        PacienteResponseDTO pacienteDto = PacienteResponseDTO.fromEntity(paciente);

        List<AgendamentoResponseDTO> agendamentos = agendamentoRepository
                .findAllByPacienteIdPacienteOrderByDataHoraSessaoDesc(idPaciente)
                .stream()
                .map(AgendamentoResponseDTO::fromEntity)
                .toList();

        List<ProntuarioClinicoResponseDTO> prontuarios = prontuarioRepository
                .findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataRegistroDesc(idPaciente)
                .stream()
                .map(ProntuarioClinicoResponseDTO::fromEntity)
                .toList();

        List<PagamentoResponseDTO> pagamentos = pagamentoRepository
                .findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataPagamentoDesc(idPaciente)
                .stream()
                .map(PagamentoResponseDTO::fromEntity)
                .toList();

        String conteudoCsv = csvView.gerarCsv(pacienteDto, agendamentos, prontuarios, pagamentos);
        String pdfABase64 = pdfAView.gerarPdfABase64(pacienteDto, agendamentos, prontuarios);

        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.EXPORTACAO,
                "EXPORTACAO_PORTABILIDADE_LGPD:PACIENTE:" + idPaciente
        );

        return new PacienteExportacaoDTO(
                Instant.now(),
                "ISO 19005-1 (PDF/A-1b) & RFC 4180 (CSV) & JSON",
                pacienteDto,
                agendamentos,
                prontuarios,
                pagamentos,
                conteudoCsv,
                "dossie_paciente_" + idPaciente + "_pdfa.pdf",
                pdfABase64
        );
    }

    @Transactional
    public byte[] exportarPdfABinario(Long idPaciente, String enderecoIp) {
        Paciente paciente = pacienteService.buscarEntidadePorId(idPaciente);
        PacienteResponseDTO pacienteDto = PacienteResponseDTO.fromEntity(paciente);

        List<AgendamentoResponseDTO> agendamentos = agendamentoRepository
                .findAllByPacienteIdPacienteOrderByDataHoraSessaoDesc(idPaciente)
                .stream()
                .map(AgendamentoResponseDTO::fromEntity)
                .toList();

        List<ProntuarioClinicoResponseDTO> prontuarios = prontuarioRepository
                .findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataRegistroDesc(idPaciente)
                .stream()
                .map(ProntuarioClinicoResponseDTO::fromEntity)
                .toList();

        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.EXPORTACAO,
                "EXPORTACAO_PDFA_LGPD:PACIENTE:" + idPaciente
        );

        return pdfAView.gerarPdfABytes(pacienteDto, agendamentos, prontuarios);
    }
}
