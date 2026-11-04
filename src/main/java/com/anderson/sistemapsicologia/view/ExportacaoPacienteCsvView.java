package com.anderson.sistemapsicologia.view;

import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoResponseDTO;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteResponseDTO;
import com.anderson.sistemapsicologia.model.pagamento.dtos.PagamentoResponseDTO;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente da camada View responsável por renderizar os dados do paciente em formato aberto CSV (RFC 4180)
 * para atender ao requisito de portabilidade da LGPD sem aprisionamento tecnológico.
 */
@Component
public class ExportacaoPacienteCsvView {

    public String gerarCsv(
            PacienteResponseDTO paciente,
            List<AgendamentoResponseDTO> agendamentos,
            List<ProntuarioClinicoResponseDTO> prontuarios,
            List<PagamentoResponseDTO> pagamentos
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("SECAO,ID,DATA_REFERENCIA,DESCRICAO_PRINCIPAL,DETALHE_SECUNDARIO,VALOR_OU_STATUS\n");

        sb.append("PACIENTE,")
                .append(paciente.idPaciente()).append(",")
                .append(paciente.dataNascimento()).append(",")
                .append(escaparCsv(paciente.nome() + " (CPF: " + paciente.cpf() + ")")).append(",")
                .append(escaparCsv("Emergencia: " + paciente.contatoEmergenciaNome() + " - " + paciente.contatoEmergenciaTelefone()
                        + " | Local: " + paciente.enderecoLocalSessao())).append(",")
                .append(paciente.ativo() ? "ATIVO" : "INATIVO")
                .append("\n");

        for (AgendamentoResponseDTO ag : agendamentos) {
            sb.append("AGENDAMENTO,")
                    .append(ag.idAgendamento()).append(",")
                    .append(ag.dataHoraSessao()).append(",")
                    .append(escaparCsv("Modalidade: " + ag.modalidade() + " | Psicologo: " + ag.nomePsicologo()
                            + " (e-Psi: " + ag.numeroEpsiPsicologo() + ")")).append(",")
                    .append(escaparCsv("Local Sessao: " + ag.enderecoLocalSessao())).append(",")
                    .append(ag.status())
                    .append("\n");
        }

        for (ProntuarioClinicoResponseDTO pr : prontuarios) {
            sb.append("PRONTUARIO_CLINICO,")
                    .append(pr.idProntuario()).append(",")
                    .append(pr.dataRegistro()).append(",")
                    .append(escaparCsv(pr.procedimentosTecnicos())).append(",")
                    .append(escaparCsv(pr.evolucaoObservavel())).append(",")
                    .append("ACESSIVEL_PACIENTE")
                    .append("\n");
        }

        for (PagamentoResponseDTO pg : pagamentos) {
            sb.append("PAGAMENTO_SPLIT,")
                    .append(pg.idPagamento()).append(",")
                    .append(pg.dataPagamento()).append(",")
                    .append(escaparCsv("Total: R$ " + pg.valorTotal())).append(",")
                    .append(escaparCsv("Taxa Clinica: R$ " + pg.taxaClinicaInfraestrutura()
                            + " | Repasse Psicologo: R$ " + pg.repasseProfissional())).append(",")
                    .append(pg.statusSplit())
                    .append("\n");
        }

        return sb.toString();
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "\"\"";
        }
        return "\"" + valor.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ") + "\"";
    }
}
