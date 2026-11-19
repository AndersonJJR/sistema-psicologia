package com.anderson.sistemapsicologia.model.dashboard;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoRepository;
import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import com.anderson.sistemapsicologia.model.dashboard.dtos.DashboardResumoResponseDTO;
import com.anderson.sistemapsicologia.model.dashboard.dtos.DashboardResumoResponseDTO.AgendamentoHojeItemDTO;
import com.anderson.sistemapsicologia.model.dashboard.dtos.DashboardResumoResponseDTO.AtendimentoRecenteDTO;
import com.anderson.sistemapsicologia.model.dashboard.dtos.DashboardResumoResponseDTO.ProximaConsultaResumoDTO;
import com.anderson.sistemapsicologia.model.paciente.Paciente;
import com.anderson.sistemapsicologia.model.paciente.PacienteRepository;
import com.anderson.sistemapsicologia.model.pagamento.Pagamento;
import com.anderson.sistemapsicologia.model.pagamento.PagamentoRepository;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinico;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinicoRepository;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoRepository agendamentoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ProntuarioClinicoRepository prontuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final AutenticacaoService autenticacaoService;

    @Transactional(readOnly = true)
    public DashboardResumoResponseDTO obterResumo() {
        Optional<UUID> idPsicologoOpt = autenticacaoService.obterIdPsicologoAutenticado();
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        YearMonth mesAnterior = mesAtual.minusMonths(1);

        List<Agendamento> todosAgendamentos = agendamentoRepository.findAllByAtivoTrueOrderByDataHoraSessaoDesc()
                .stream()
                .filter(a -> idPsicologoOpt.isEmpty() || a.getPsicologo().getIdPsicologo().equals(idPsicologoOpt.get()))
                .toList();

        List<Pagamento> todosPagamentos = pagamentoRepository.findAllByAtivoTrueOrderByDataPagamentoDesc()
                .stream()
                .filter(p -> idPsicologoOpt.isEmpty()
                        || p.getAgendamento().getPsicologo().getIdPsicologo().equals(idPsicologoOpt.get()))
                .toList();

        List<ProntuarioClinico> todosProntuarios = prontuarioRepository.findAllByAtivoTrueOrderByDataRegistroDesc()
                .stream()
                .filter(pr -> idPsicologoOpt.isEmpty()
                        || pr.getAgendamento().getPsicologo().getIdPsicologo().equals(idPsicologoOpt.get()))
                .toList();

        // 1. Financeiro do mês atual e variação vs mês anterior
        List<Pagamento> pagamentosMesAtual = todosPagamentos.stream()
                .filter(p -> p.getDataPagamento() != null
                        && YearMonth.from(p.getDataPagamento().atZone(ZoneId.systemDefault())).equals(mesAtual))
                .toList();

        BigDecimal repasseMesAtual = pagamentosMesAtual.stream()
                .map(Pagamento::getRepasseProfissional)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal brutoMesAtual = pagamentosMesAtual.stream()
                .map(Pagamento::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal retencaoMesAtual = pagamentosMesAtual.stream()
                .map(Pagamento::getTaxaClinicaInfraestrutura)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal repasseMesAnterior = todosPagamentos.stream()
                .filter(p -> p.getDataPagamento() != null
                        && YearMonth.from(p.getDataPagamento().atZone(ZoneId.systemDefault())).equals(mesAnterior))
                .map(Pagamento::getRepasseProfissional)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double variacaoPercentual = 0.0;
        if (repasseMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            variacaoPercentual = repasseMesAtual.subtract(repasseMesAnterior)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(repasseMesAnterior, 1, RoundingMode.HALF_UP)
                    .doubleValue();
        } else if (repasseMesAtual.compareTo(BigDecimal.ZERO) > 0) {
            variacaoPercentual = 100.0;
        }

        List<BigDecimal> sparkline = todosPagamentos.stream()
                .sorted(Comparator.comparing(Pagamento::getDataPagamento))
                .map(Pagamento::getRepasseProfissional)
                .limit(8)
                .toList();

        // 2. Agendamentos de Hoje
        Map<UUID, Long> sessoesPorPaciente = new HashMap<>();
        for (Agendamento a : todosAgendamentos) {
            if (a.getStatus() != StatusAgendamento.CANCELADO) {
                sessoesPorPaciente.merge(a.getPaciente().getIdPaciente(), 1L, Long::sum);
            }
        }

        List<Agendamento> agendamentosHojeEntidades = todosAgendamentos.stream()
                .filter(a -> a.getDataHoraSessao().toLocalDate().equals(hoje)
                        && a.getStatus() != StatusAgendamento.CANCELADO)
                .sorted(Comparator.comparing(Agendamento::getDataHoraSessao))
                .toList();

        long totalConsultasHoje = agendamentosHojeEntidades.size();
        long totalTelessaudeHoje = agendamentosHojeEntidades.stream()
                .filter(a -> a.getModalidade() == ModalidadeAtendimento.TELECONSULTA)
                .count();
        long totalPresenciaisHoje = totalConsultasHoje - totalTelessaudeHoje;

        Optional<Agendamento> proximaOpt = agendamentosHojeEntidades.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.AGENDADO && !a.getDataHoraSessao().isBefore(agora.minusMinutes(15)))
                .findFirst();

        ProximaConsultaResumoDTO proximaResumo = proximaOpt.map(prox -> new ProximaConsultaResumoDTO(
                prox.getIdAgendamento(),
                prox.getDataHoraSessao().format(HORA_FORMATTER),
                Math.max(0L, Duration.between(agora, prox.getDataHoraSessao()).toMinutes())
        )).orElse(null);

        List<AgendamentoHojeItemDTO> agendamentosHojeDtos = agendamentosHojeEntidades.stream()
                .map(a -> {
                    Paciente pac = a.getPaciente();
                    int idade = pac.getDataNascimento() != null
                            ? java.time.Period.between(pac.getDataNascimento(), hoje).getYears()
                            : 0;
                    String abordagem = a.getPsicologo().getAbordagemClinica() != null
                            ? a.getPsicologo().getAbordagemClinica()
                            : "tcc";
                    return new AgendamentoHojeItemDTO(
                            a.getIdAgendamento(),
                            pac.getIdPaciente(),
                            pac.getNome(),
                            pac.getAvatarUrl(),
                            Math.max(0, idade),
                            abordagem,
                            sessoesPorPaciente.getOrDefault(pac.getIdPaciente(), 1L),
                            a.getDataHoraSessao(),
                            a.getDuracaoMinutos() != null ? a.getDuracaoMinutos() : 50,
                            a.getModalidade(),
                            a.getStatus(),
                            a.getEnderecoLocalSessao(),
                            a.getTipoSessao() != null ? a.getTipoSessao() : "Sessão Individual Ordinária"
                    );
                })
                .toList();

        // 3. Assiduidade e Pacientes Ativos
        long realizadas = todosAgendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.REALIZADO)
                .count();
        long faltas = todosAgendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.FALTA)
                .count();
        double taxaAssiduidade = (realizadas + faltas) > 0
                ? BigDecimal.valueOf((realizadas * 100.0) / (realizadas + faltas))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue()
                : 100.0;

        long pacientesAtivos = pacienteRepository.findAllByAtivoTrue().size();

        // 4. Pendências Regulatórias (prontuários em rascunho)
        long prontuariosEmRascunho = todosProntuarios.stream()
                .filter(pr -> !pr.isAssinado())
                .count();

        // 5. Atendimentos Recentes (últimos 4 realizados ou passados)
        Map<UUID, ProntuarioClinico> prontuarioPorAgendamento = new HashMap<>();
        for (ProntuarioClinico pr : todosProntuarios) {
            prontuarioPorAgendamento.putIfAbsent(pr.getAgendamento().getIdAgendamento(), pr);
        }

        Map<UUID, Pagamento> pagamentoPorAgendamento = new HashMap<>();
        for (Pagamento pg : todosPagamentos) {
            pagamentoPorAgendamento.putIfAbsent(pg.getAgendamento().getIdAgendamento(), pg);
        }

        List<AtendimentoRecenteDTO> atendimentosRecentes = todosAgendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.REALIZADO
                        || prontuarioPorAgendamento.containsKey(a.getIdAgendamento()))
                .limit(4)
                .map(a -> {
                    ProntuarioClinico pr = prontuarioPorAgendamento.get(a.getIdAgendamento());
                    Pagamento pg = pagamentoPorAgendamento.get(a.getIdAgendamento());

                    String prontuarioStatus = "PENDENTE";
                    if (pr != null) {
                        prontuarioStatus = pr.isAssinado() ? "ASSINADO" : "RASCUNHO";
                    }

                    return new AtendimentoRecenteDTO(
                            a.getIdAgendamento(),
                            a.getPaciente().getIdPaciente(),
                            a.getPaciente().getNome(),
                            a.getModalidade(),
                            a.getDataHoraSessao(),
                            prontuarioStatus,
                            pr != null ? pr.getDataRegistro() : null,
                            pg != null ? pg.getValorTotal() : null,
                            pg != null ? pg.getMeioPagamento() : null,
                            pg != null ? pg.getStatusSplit() : null
                    );
                })
                .toList();

        return new DashboardResumoResponseDTO(
                repasseMesAtual,
                brutoMesAtual,
                retencaoMesAtual,
                variacaoPercentual,
                sparkline,
                totalConsultasHoje,
                totalTelessaudeHoje,
                totalPresenciaisHoje,
                proximaResumo,
                taxaAssiduidade,
                pacientesAtivos,
                prontuariosEmRascunho,
                agendamentosHojeDtos,
                atendimentosRecentes
        );
    }
}
