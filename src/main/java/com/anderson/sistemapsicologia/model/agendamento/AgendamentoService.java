package com.anderson.sistemapsicologia.model.agendamento;

import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoRequestDTO;
import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoResponseDTO;
import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.paciente.Paciente;
import com.anderson.sistemapsicologia.model.paciente.PacienteService;
import com.anderson.sistemapsicologia.model.pagamento.CalculadoraSplitPagamento;
import com.anderson.sistemapsicologia.model.pagamento.Pagamento;
import com.anderson.sistemapsicologia.model.pagamento.PagamentoRepository;
import com.anderson.sistemapsicologia.model.pagamento.StatusSplit;
import com.anderson.sistemapsicologia.model.psicologo.Psicologo;
import com.anderson.sistemapsicologia.model.psicologo.PsicologoService;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private static final BigDecimal PERCENTUAL_TAXA_CLINICA = new BigDecimal("0.30");

    private final AgendamentoRepository agendamentoRepository;
    private final PsicologoService psicologoService;
    private final PacienteService pacienteService;
    private final PagamentoRepository pagamentoRepository;
    private final CalculadoraSplitPagamento calculadoraSplit;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public AgendamentoResponseDTO criar(AgendamentoRequestDTO dto, String enderecoIp) {
        Psicologo psicologo = psicologoService.buscarEntidadeAtiva(dto.idPsicologo());
        Paciente paciente = pacienteService.buscarEntidadeAtiva(dto.idPaciente());

        String localSessao = dto.enderecoLocalSessao() != null && !dto.enderecoLocalSessao().isBlank()
                ? dto.enderecoLocalSessao()
                : paciente.getEnderecoLocalSessao();

        if (dto.modalidade() == ModalidadeAtendimento.TELECONSULTA) {
            if (localSessao == null || localSessao.isBlank()) {
                throw new RegraNegocioException(
                        "Teleconsulta exige o endereço físico onde o paciente se encontra para gestão de crises."
                );
            }
        }

        int duracao = dto.duracaoMinutos() != null && dto.duracaoMinutos() > 0
                ? dto.duracaoMinutos()
                : 50;
        String tipoSessao = dto.tipoSessao() != null && !dto.tipoSessao().isBlank()
                ? dto.tipoSessao().trim()
                : "Sessão Individual Ordinária";
        boolean enviarLembrete = dto.enviarLembrete24h() == null || dto.enviarLembrete24h();

        int totalOcorrencias = Boolean.TRUE.equals(dto.repetirSemanalmente())
                ? Math.max(2, Math.min(12, dto.quantidadeSemanas() != null ? dto.quantidadeSemanas() : 4))
                : 1;

        Agendamento principalSalvo = null;
        for (int semana = 0; semana < totalOcorrencias; semana++) {
            LocalDateTime dataOcorrencia = dto.dataHoraSessao().plusWeeks(semana);
            if (agendamentoRepository.existsByPsicologoIdPsicologoAndDataHoraSessaoAndAtivoTrue(
                    psicologo.getIdPsicologo(),
                    dataOcorrencia
            )) {
                throw new RegraNegocioException(
                        "O psicólogo já possui outro agendamento ativo neste mesmo horário (" + dataOcorrencia + ")."
                );
            }

            Agendamento agendamento = Agendamento.builder()
                    .psicologo(psicologo)
                    .paciente(paciente)
                    .dataHoraSessao(dataOcorrencia)
                    .modalidade(dto.modalidade())
                    .status(dto.status() != null ? dto.status() : StatusAgendamento.AGENDADO)
                    .enderecoLocalSessao(localSessao)
                    .duracaoMinutos(duracao)
                    .tipoSessao(tipoSessao)
                    .enviarLembrete24h(enviarLembrete)
                    .build();

            Agendamento salvo = agendamentoRepository.save(agendamento);
            if (principalSalvo == null) {
                principalSalvo = salvo;
            }

            auditoriaService.registrarAcao(
                    autenticacaoService.obterLoginOperadorAtual(),
                    enderecoIp,
                    AcaoAuditoria.INSERCAO,
                    "AGENDAMENTO:" + salvo.getIdAgendamento()
            );

            if (dto.valorHonorario() != null && dto.valorHonorario().compareTo(BigDecimal.ZERO) > 0) {
                registrarSplitAutomatico(salvo, dto.valorHonorario(), enderecoIp);
            }
        }

        return AgendamentoResponseDTO.fromEntity(principalSalvo);
    }

    private void registrarSplitAutomatico(Agendamento agendamento, BigDecimal valorHonorario, String enderecoIp) {
        BigDecimal total = valorHonorario.setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxaClinica = total.multiply(PERCENTUAL_TAXA_CLINICA).setScale(2, RoundingMode.HALF_UP);
        CalculadoraSplitPagamento.ResultadoSplit split = calculadoraSplit.processarSplit(
                total,
                taxaClinica,
                null
        );

        Pagamento pagamento = Pagamento.builder()
                .agendamento(agendamento)
                .valorTotal(split.valorTotal())
                .taxaClinicaInfraestrutura(split.taxaClinicaInfraestrutura())
                .repasseProfissional(split.repasseProfissional())
                .statusSplit(StatusSplit.PENDENTE)
                .meioPagamento("PIX")
                .dataPagamento(Instant.now())
                .build();

        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.INSERCAO,
                "PAGAMENTO:SPLIT_AUTOMATICO:" + pagamentoSalvo.getIdPagamento()
        );
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarAtivos() {
        return agendamentoRepository.findAllByAtivoTrueOrderByDataHoraSessaoDesc()
                .stream()
                .map(AgendamentoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Agendamento buscarEntidadeAtiva(UUID id) {
        return agendamentoRepository.findByIdAgendamentoAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado ou inativo: " + id));
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(UUID id, String enderecoIp) {
        Agendamento agendamento = buscarEntidadeAtiva(id);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "AGENDAMENTO:" + id
        );
        return AgendamentoResponseDTO.fromEntity(agendamento);
    }

    @Transactional
    public AgendamentoResponseDTO atualizarStatus(
            UUID id,
            StatusAgendamento novoStatus,
            String enderecoIp
    ) {
        Agendamento agendamento = buscarEntidadeAtiva(id);
        agendamento.setStatus(novoStatus);
        Agendamento salvo = agendamentoRepository.save(agendamento);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "AGENDAMENTO:STATUS_" + novoStatus.name() + ":" + id
        );
        return AgendamentoResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public AgendamentoResponseDTO acionarProtocoloEmergencia(UUID id, String enderecoIp) {
        Agendamento agendamento = buscarEntidadeAtiva(id);
        Paciente paciente = agendamento.getPaciente();
        if (paciente.getContatoEmergenciaTelefone() == null || paciente.getContatoEmergenciaTelefone().isBlank()) {
            throw new RegraNegocioException(
                    "O paciente não possui telefone de contato de emergência cadastrado."
            );
        }
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "AGENDAMENTO:PROTOCOLO_EMERGENCIA_ACIONADO:" + id
        );
        return AgendamentoResponseDTO.fromEntity(agendamento);
    }

    @Transactional
    public void inativar(UUID id, String enderecoIp) {
        Agendamento agendamento = buscarEntidadeAtiva(id);
        agendamento.realizarSoftDelete();
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "AGENDAMENTO:SOFT_DELETE:" + id
        );
    }
}
