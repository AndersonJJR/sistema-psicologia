package com.anderson.sistemapsicologia.model.registrodocumental;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoService;
import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.AcessoNegadoClinicoException;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.registrodocumental.dtos.RegistroDocumentalRequestDTO;
import com.anderson.sistemapsicologia.model.registrodocumental.dtos.RegistroDocumentalResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço para manipulação de REGISTROS_DOCUMENTAIS.
 * Aplica isolamento estrito de titularidade: apenas o psicólogo titular (identificado via JWT)
 * pode consultar ou gravar registros documentais de seus próprios atendimentos (Resolução CFP nº 001/2009).
 */
@Service
@RequiredArgsConstructor
public class RegistroDocumentalService {

    private final RegistroDocumentalRepository registroDocumentalRepository;
    private final AgendamentoService agendamentoService;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public RegistroDocumentalResponseDTO criar(RegistroDocumentalRequestDTO dto, String enderecoIp) {
        UUID idPsicologoJwt = exigirPsicologoTitularAutenticado();

        Agendamento agendamento = agendamentoService.buscarEntidadeAtiva(dto.idAgendamento());
        validarTitularidade(agendamento.getPsicologo().getIdPsicologo(), idPsicologoJwt, enderecoIp, "AGENDAMENTO:" + dto.idAgendamento());

        Optional<RegistroDocumental> existenteOpt = registroDocumentalRepository
                .findByAgendamentoIdAgendamentoAndAtivoTrue(dto.idAgendamento());

        if (existenteOpt.isPresent()) {
            RegistroDocumental existente = existenteOpt.get();
            if (existente.isAssinado()) {
                throw new RegraNegocioException(
                        "O registro documental desta sessão já foi selado por assinatura digital e é imutável."
                );
            }
            existente.setHipotesesProvisorias(dto.hipotesesProvisorias());
            existente.setNotasSupervisao(dto.notasSupervisao());
            existente.setDataRegistro(Instant.now());
            RegistroDocumental atualizado = registroDocumentalRepository.save(existente);
            auditoriaService.registrarAcao(
                    autenticacaoService.obterLoginOperadorAtual(),
                    enderecoIp,
                    AcaoAuditoria.ALTERACAO,
                    "REGISTRO_DOCUMENTAL:RASCUNHO:" + atualizado.getIdRegistroDocumental()
            );
            return RegistroDocumentalResponseDTO.fromEntity(atualizado);
        }

        RegistroDocumental registro = RegistroDocumental.builder()
                .agendamento(agendamento)
                .hipotesesProvisorias(dto.hipotesesProvisorias())
                .notasSupervisao(dto.notasSupervisao())
                .dataRegistro(Instant.now())
                .assinado(false)
                .build();

        RegistroDocumental salvo = registroDocumentalRepository.save(registro);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.INSERCAO,
                "REGISTRO_DOCUMENTAL:" + salvo.getIdRegistroDocumental()
        );
        return RegistroDocumentalResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public RegistroDocumentalResponseDTO buscarPorAgendamento(UUID idAgendamento, String enderecoIp) {
        UUID idPsicologoJwt = exigirPsicologoTitularAutenticado();
        RegistroDocumental registro = registroDocumentalRepository
                .findByAgendamentoIdAgendamentoAndAtivoTrue(idAgendamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Registro documental não encontrado para o agendamento: " + idAgendamento
                ));
        validarTitularidade(
                registro.getAgendamento().getPsicologo().getIdPsicologo(),
                idPsicologoJwt,
                enderecoIp,
                "REGISTRO_DOCUMENTAL:AGENDAMENTO:" + idAgendamento
        );
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "REGISTRO_DOCUMENTAL:AGENDAMENTO:" + idAgendamento
        );
        return RegistroDocumentalResponseDTO.fromEntity(registro);
    }

    @Transactional(readOnly = true)
    public List<RegistroDocumentalResponseDTO> listarMeusRegistros(String enderecoIp) {
        UUID idPsicologoJwt = exigirPsicologoTitularAutenticado();
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "REGISTRO_DOCUMENTAL:LISTA_TITULAR:" + idPsicologoJwt
        );
        return registroDocumentalRepository
                .findAllByAgendamentoPsicologoIdPsicologoAndAtivoTrueOrderByDataRegistroDesc(idPsicologoJwt)
                .stream()
                .map(RegistroDocumentalResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegistroDocumentalResponseDTO buscarPorId(UUID idRegistro, String enderecoIp) {
        UUID idPsicologoJwt = exigirPsicologoTitularAutenticado();
        RegistroDocumental registro = registroDocumentalRepository.findByIdRegistroDocumentalAndAtivoTrue(idRegistro)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro documental não encontrado: " + idRegistro));

        validarTitularidade(
                registro.getAgendamento().getPsicologo().getIdPsicologo(),
                idPsicologoJwt,
                enderecoIp,
                "REGISTRO_DOCUMENTAL:" + idRegistro
        );

        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "REGISTRO_DOCUMENTAL:" + idRegistro
        );
        return RegistroDocumentalResponseDTO.fromEntity(registro);
    }

    @Transactional
    public void inativar(UUID idRegistro, String enderecoIp) {
        UUID idPsicologoJwt = exigirPsicologoTitularAutenticado();
        RegistroDocumental registro = registroDocumentalRepository.findByIdRegistroDocumentalAndAtivoTrue(idRegistro)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro documental não encontrado: " + idRegistro));

        validarTitularidade(
                registro.getAgendamento().getPsicologo().getIdPsicologo(),
                idPsicologoJwt,
                enderecoIp,
                "REGISTRO_DOCUMENTAL:SOFT_DELETE:" + idRegistro
        );

        registro.realizarSoftDelete();
        registroDocumentalRepository.save(registro);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "REGISTRO_DOCUMENTAL:SOFT_DELETE:" + idRegistro
        );
    }

    private UUID exigirPsicologoTitularAutenticado() {
        return autenticacaoService.obterIdPsicologoAutenticado()
                .orElseThrow(() -> new AcessoNegadoClinicoException(
                        "Apenas o psicólogo titular autenticado via JWT pode acessar REGISTROS_DOCUMENTAIS."
                ));
    }

    private void validarTitularidade(UUID idPsicologoDono, UUID idPsicologoRequisitante, String ip, String recurso) {
        if (!idPsicologoDono.equals(idPsicologoRequisitante)) {
            auditoriaService.registrarAcao(
                    autenticacaoService.obterLoginOperadorAtual(),
                    ip,
                    AcaoAuditoria.LEITURA,
                    "BLOQUEIO_SIGILO_CFP:" + recurso
            );
            throw new AcessoNegadoClinicoException(
                    "Violação de sigilo profissional (CFP 001/2009): acesso restrito exclusivamente ao psicólogo titular do atendimento."
            );
        }
    }
}
