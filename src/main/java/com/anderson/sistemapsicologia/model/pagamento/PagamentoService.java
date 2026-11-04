package com.anderson.sistemapsicologia.model.pagamento;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoService;
import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.pagamento.dtos.PagamentoRequestDTO;
import com.anderson.sistemapsicologia.model.pagamento.dtos.PagamentoResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final AgendamentoService agendamentoService;
    private final CalculadoraSplitPagamento calculadoraSplit;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public PagamentoResponseDTO registrarPagamento(PagamentoRequestDTO dto, String enderecoIp) {
        if (pagamentoRepository.existsByAgendamentoIdAgendamento(dto.idAgendamento())) {
            throw new RegraNegocioException("Já existe pagamento registrado para este agendamento.");
        }

        Agendamento agendamento = agendamentoService.buscarEntidadeAtiva(dto.idAgendamento());
        CalculadoraSplitPagamento.ResultadoSplit split = calculadoraSplit.processarSplit(
                dto.valorTotal(),
                dto.taxaClinicaInfraestrutura(),
                dto.repasseProfissional()
        );

        Pagamento pagamento = Pagamento.builder()
                .agendamento(agendamento)
                .valorTotal(split.valorTotal())
                .taxaClinicaInfraestrutura(split.taxaClinicaInfraestrutura())
                .repasseProfissional(split.repasseProfissional())
                .statusSplit(dto.statusSplit() != null ? dto.statusSplit() : StatusSplit.PENDENTE)
                .dataPagamento(Instant.now())
                .build();

        Pagamento salvo = pagamentoRepository.save(pagamento);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.INSERCAO,
                "PAGAMENTO:" + salvo.getIdPagamento()
        );
        return PagamentoResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public PagamentoResponseDTO liquidarSplit(UUID idPagamento, String enderecoIp) {
        Pagamento pagamento = pagamentoRepository.findByIdPagamentoAndAtivoTrue(idPagamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado: " + idPagamento));

        pagamento.setStatusSplit(StatusSplit.LIQUIDADO);
        Pagamento salvo = pagamentoRepository.save(pagamento);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "PAGAMENTO:LIQUIDAR_SPLIT:" + idPagamento
        );
        return PagamentoResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarAtivos() {
        return pagamentoRepository.findAllByAtivoTrueOrderByDataPagamentoDesc()
                .stream()
                .map(PagamentoResponseDTO::fromEntity)
                .toList();
    }
}
