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

        String meio = dto.meioPagamento() != null && !dto.meioPagamento().isBlank()
                ? dto.meioPagamento().trim().toUpperCase()
                : "PIX";

        Pagamento pagamento = Pagamento.builder()
                .agendamento(agendamento)
                .valorTotal(split.valorTotal())
                .taxaClinicaInfraestrutura(split.taxaClinicaInfraestrutura())
                .repasseProfissional(split.repasseProfissional())
                .statusSplit(dto.statusSplit() != null ? dto.statusSplit() : StatusSplit.PENDENTE)
                .meioPagamento(meio)
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

    @Transactional(readOnly = true)
    public String gerarCsvCarneLeao(String enderecoIp) {
        List<Pagamento> pagamentos = pagamentoRepository.findAllByAtivoTrueOrderByDataPagamentoDesc();
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.EXPORTACAO,
                "PAGAMENTO:EXPORTACAO_CARNE_LEAO_CSV"
        );
        StringBuilder sb = new StringBuilder();
        sb.append("Data_Pagamento;CPF_Titular;Nome_Paciente;Valor_Bruto;Retencao_Clinica_30;Repasse_Profissional_Tributavel_70;Meio_Pagamento;Status_Split\n");
        for (Pagamento p : pagamentos) {
            sb.append(p.getDataPagamento()).append(";")
                    .append(p.getAgendamento().getPaciente().getCpf()).append(";")
                    .append(p.getAgendamento().getPaciente().getNome()).append(";")
                    .append(p.getValorTotal()).append(";")
                    .append(p.getTaxaClinicaInfraestrutura()).append(";")
                    .append(p.getRepasseProfissional()).append(";")
                    .append(p.getMeioPagamento() != null ? p.getMeioPagamento() : "PIX").append(";")
                    .append(p.getStatusSplit().name()).append("\n");
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String gerarXmlSplitPagamento(UUID idPagamento, String enderecoIp) {
        Pagamento p = idPagamento != null
                ? pagamentoRepository.findByIdPagamentoAndAtivoTrue(idPagamento)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado: " + idPagamento))
                : pagamentoRepository.findAllByAtivoTrueOrderByDataPagamentoDesc()
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum pagamento registrado para exportação XML."));

        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.EXPORTACAO,
                "PAGAMENTO:COMPROVANTE_XML:" + p.getIdPagamento()
        );

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ComprovanteSplitPagamento versao=\"1.0\" padrao=\"ReceitaFederal-NFSe\">\n" +
                "  <IdTransacao>" + p.getIdPagamento() + "</IdTransacao>\n" +
                "  <DataLiquidacao>" + p.getDataPagamento() + "</DataLiquidacao>\n" +
                "  <MeioPagamento>" + (p.getMeioPagamento() != null ? p.getMeioPagamento() : "PIX") + "</MeioPagamento>\n" +
                "  <StatusSplit>" + p.getStatusSplit().name() + "</StatusSplit>\n" +
                "  <Paciente>\n" +
                "    <Nome>" + p.getAgendamento().getPaciente().getNome() + "</Nome>\n" +
                "    <CPF>" + p.getAgendamento().getPaciente().getCpf() + "</CPF>\n" +
                "  </Paciente>\n" +
                "  <PsicologoTitular>\n" +
                "    <Nome>" + p.getAgendamento().getPsicologo().getNome() + "</Nome>\n" +
                "    <CRP>" + p.getAgendamento().getPsicologo().getCrp() + "</CRP>\n" +
                "  </PsicologoTitular>\n" +
                "  <PartilhaFinanceira>\n" +
                "    <ValorBrutoSessao>" + p.getValorTotal() + "</ValorBrutoSessao>\n" +
                "    <RetencaoClinicaInfraestrutura>" + p.getTaxaClinicaInfraestrutura() + "</RetencaoClinicaInfraestrutura>\n" +
                "    <RepasseLiquidoProfissional>" + p.getRepasseProfissional() + "</RepasseLiquidoProfissional>\n" +
                "  </PartilhaFinanceira>\n" +
                "</ComprovanteSplitPagamento>\n";
    }
}
