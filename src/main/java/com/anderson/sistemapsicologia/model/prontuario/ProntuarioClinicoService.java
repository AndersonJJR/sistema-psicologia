package com.anderson.sistemapsicologia.model.prontuario;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoService;
import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoRequestDTO;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProntuarioClinicoService {

    private final ProntuarioClinicoRepository prontuarioRepository;
    private final AgendamentoService agendamentoService;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public ProntuarioClinicoResponseDTO criar(ProntuarioClinicoRequestDTO dto, String enderecoIp) {
        if (prontuarioRepository.existsByAgendamentoIdAgendamento(dto.idAgendamento())) {
            throw new RegraNegocioException("Já existe prontuário clínico registrado para o agendamento informado.");
        }

        Agendamento agendamento = agendamentoService.buscarEntidadeAtiva(dto.idAgendamento());

        ProntuarioClinico prontuario = ProntuarioClinico.builder()
                .agendamento(agendamento)
                .procedimentosTecnicos(dto.procedimentosTecnicos())
                .evolucaoObservavel(dto.evolucaoObservavel())
                .dataRegistro(Instant.now())
                .build();

        ProntuarioClinico salvo = prontuarioRepository.save(prontuario);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.INSERCAO,
                "PRONTUARIO_CLINICO:" + salvo.getIdProntuario()
        );
        return ProntuarioClinicoResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<ProntuarioClinicoResponseDTO> listarAtivos(String enderecoIp) {
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "PRONTUARIO_CLINICO:LISTA"
        );
        return prontuarioRepository.findAllByAtivoTrueOrderByDataRegistroDesc()
                .stream()
                .map(ProntuarioClinicoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProntuarioClinicoResponseDTO buscarPorId(UUID id, String enderecoIp) {
        ProntuarioClinico prontuario = prontuarioRepository.findByIdProntuarioAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prontuário clínico não encontrado: " + id));
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "PRONTUARIO_CLINICO:" + id
        );
        return ProntuarioClinicoResponseDTO.fromEntity(prontuario);
    }

    @Transactional(readOnly = true)
    public List<ProntuarioClinicoResponseDTO> listarPorPaciente(UUID idPaciente, String enderecoIp) {
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "PRONTUARIO_CLINICO:PACIENTE:" + idPaciente
        );
        return prontuarioRepository.findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataRegistroDesc(idPaciente)
                .stream()
                .map(ProntuarioClinicoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void inativar(UUID id, String enderecoIp) {
        ProntuarioClinico prontuario = prontuarioRepository.findByIdProntuarioAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prontuário clínico não encontrado: " + id));
        prontuario.realizarSoftDelete();
        prontuarioRepository.save(prontuario);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "PRONTUARIO_CLINICO:SOFT_DELETE:" + id
        );
    }
}
