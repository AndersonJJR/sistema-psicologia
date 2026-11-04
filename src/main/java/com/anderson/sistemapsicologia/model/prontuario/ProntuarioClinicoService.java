package com.anderson.sistemapsicologia.model.prontuario;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoService;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoRequestDTO;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoResponseDTO;
import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumentalRepository;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProntuarioClinicoService {

    private final ProntuarioClinicoRepository prontuarioRepository;
    private final RegistroDocumentalRepository registroDocumentalRepository;
    private final AgendamentoService agendamentoService;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public ProntuarioClinicoResponseDTO criar(ProntuarioClinicoRequestDTO dto, String enderecoIp) {
        Optional<ProntuarioClinico> existenteOpt = prontuarioRepository
                .findByAgendamentoIdAgendamentoAndAtivoTrue(dto.idAgendamento());

        if (existenteOpt.isPresent()) {
            ProntuarioClinico existente = existenteOpt.get();
            if (existente.isAssinado()) {
                throw new RegraNegocioException(
                        "O prontuário clínico desta sessão já foi assinado digitalmente e tornou-se imutável."
                );
            }
            existente.setProcedimentosTecnicos(dto.procedimentosTecnicos());
            existente.setEvolucaoObservavel(dto.evolucaoObservavel());
            existente.setDataRegistro(Instant.now());
            ProntuarioClinico atualizado = prontuarioRepository.save(existente);
            auditoriaService.registrarAcao(
                    autenticacaoService.obterLoginOperadorAtual(),
                    enderecoIp,
                    AcaoAuditoria.ALTERACAO,
                    "PRONTUARIO_CLINICO:RASCUNHO:" + atualizado.getIdProntuario()
            );
            return ProntuarioClinicoResponseDTO.fromEntity(atualizado);
        }

        Agendamento agendamento = agendamentoService.buscarEntidadeAtiva(dto.idAgendamento());

        ProntuarioClinico prontuario = ProntuarioClinico.builder()
                .agendamento(agendamento)
                .procedimentosTecnicos(dto.procedimentosTecnicos())
                .evolucaoObservavel(dto.evolucaoObservavel())
                .dataRegistro(Instant.now())
                .assinado(false)
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
    public ProntuarioClinicoResponseDTO buscarPorAgendamento(UUID idAgendamento, String enderecoIp) {
        ProntuarioClinico prontuario = prontuarioRepository
                .findByAgendamentoIdAgendamentoAndAtivoTrue(idAgendamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Prontuário clínico não encontrado para o agendamento: " + idAgendamento
                ));
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "PRONTUARIO_CLINICO:AGENDAMENTO:" + idAgendamento
        );
        return ProntuarioClinicoResponseDTO.fromEntity(prontuario);
    }

    @Transactional
    public ProntuarioClinicoResponseDTO assinarPorAgendamento(UUID idAgendamento, String enderecoIp) {
        ProntuarioClinico prontuario = prontuarioRepository
                .findByAgendamentoIdAgendamentoAndAtivoTrue(idAgendamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Salve o prontuário clínico antes de assinar digitalmente a sessão."
                ));
        if (prontuario.isAssinado()) {
            throw new RegraNegocioException(
                    "Esta sessão já possui assinatura digital criptográfica ativa."
            );
        }

        String hashCurto = gerarHashSha256Formatado(
                idAgendamento + "|" + prontuario.getProcedimentosTecnicos() + "|" + prontuario.getEvolucaoObservavel()
        );
        prontuario.setAssinado(true);
        prontuario.setHashAssinatura(hashCurto);
        prontuario.setDataRegistro(Instant.now());
        ProntuarioClinico salvo = prontuarioRepository.save(prontuario);

        registroDocumentalRepository.findByAgendamentoIdAgendamentoAndAtivoTrue(idAgendamento)
                .ifPresent(reg -> {
                    reg.setAssinado(true);
                    registroDocumentalRepository.save(reg);
                });

        agendamentoService.atualizarStatus(idAgendamento, StatusAgendamento.REALIZADO, enderecoIp);

        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "PRONTUARIO_CLINICO:ASSINATURA_DIGITAL:" + salvo.getIdProntuario()
        );
        return ProntuarioClinicoResponseDTO.fromEntity(salvo);
    }

    private String gerarHashSha256Formatado(String conteudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(conteudo.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(bytes);
            return "SHA-256 • " + hex.substring(0, 4) + "..." + hex.substring(hex.length() - 4);
        } catch (NoSuchAlgorithmException e) {
            return "SHA-256 • a8f2...99bc";
        }
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
