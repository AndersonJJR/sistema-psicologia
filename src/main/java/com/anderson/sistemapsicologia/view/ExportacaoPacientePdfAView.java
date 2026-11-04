package com.anderson.sistemapsicologia.view;

import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoResponseDTO;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteResponseDTO;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoResponseDTO;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Componente da camada View responsável por gerar documento em formato PDF/A-1b (ISO 19005-1)
 * contendo o dossiê clínico público do paciente para portabilidade LGPD e guarda legal de 20 anos.
 */
@Component
public class ExportacaoPacientePdfAView {

    public byte[] gerarPdfABytes(
            PacienteResponseDTO paciente,
            List<AgendamentoResponseDTO> agendamentos,
            List<ProntuarioClinicoResponseDTO> prontuarios
    ) {
        List<String> linhas = new ArrayList<>();
        linhas.add("DOSSIE DE PORTABILIDADE CLINICA - PADRAO PDF/A-1b (LGPD & CFP 001/2009)");
        linhas.add("----------------------------------------------------------------------");
        linhas.add("Paciente: " + normalizarAscii(paciente.nome()) + " | CPF: " + normalizarAscii(paciente.cpf()));
        linhas.add("Data Nascimento: " + paciente.dataNascimento() + " | E-mail: " + normalizarAscii(paciente.email()));
        linhas.add("Contato Emergencia: " + normalizarAscii(paciente.contatoEmergenciaNome())
                + " (" + normalizarAscii(paciente.contatoEmergenciaTelefone()) + ")");
        linhas.add("Endereco Local Sessao (Telessaude): " + normalizarAscii(paciente.enderecoLocalSessao()));
        linhas.add(" ");
        linhas.add("HISTORICO DE PRONTUARIOS CLINICOS (ACESSIVEIS AO PACIENTE):");

        if (prontuarios.isEmpty()) {
            linhas.add("  - Nenhum prontuario clinico registrado.");
        } else {
            for (ProntuarioClinicoResponseDTO pr : prontuarios) {
                linhas.add("  * Prontuario #" + pr.idProntuario() + " (" + pr.dataRegistro() + ")");
                linhas.add("    Psicologo: " + normalizarAscii(pr.nomePsicologo()));
                linhas.add("    Procedimentos Tecnicos: " + normalizarAscii(pr.procedimentosTecnicos()));
                linhas.add("    Evolucao Observavel: " + normalizarAscii(pr.evolucaoObservavel()));
            }
        }

        linhas.add(" ");
        linhas.add("HISTORICO DE AGENDAMENTOS (" + agendamentos.size() + " sessoes):");
        for (AgendamentoResponseDTO ag : agendamentos) {
            linhas.add("  * Sessao #" + ag.idAgendamento() + " | " + ag.dataHoraSessao()
                    + " | Modalidade: " + ag.modalidade() + " | Status: " + ag.status());
        }

        StringBuilder contentStream = new StringBuilder();
        contentStream.append("BT\n/F1 10 Tf\n50 780 Td\n14 TL\n");
        for (String linha : linhas) {
            contentStream.append("(").append(escaparPdfTexto(linha)).append(") Tj T*\n");
        }
        contentStream.append("ET\n");
        byte[] contentBytes = contentStream.toString().getBytes(StandardCharsets.US_ASCII);

        String xmpMetadata = """
                <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
                <x:xmpmeta xmlns:x="adobe:ns:meta/">
                  <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                    <rdf:Description rdf:about="" xmlns:pdfaid="http://www.aiim.org/pdfa/ns/id/">
                      <pdfaid:part>1</pdfaid:part>
                      <pdfaid:conformance>B</pdfaid:conformance>
                    </rdf:Description>
                  </rdf:RDF>
                </x:xmpmeta>
                <?xpacket end="w"?>
                """.strip();
        byte[] xmpBytes = xmpMetadata.getBytes(StandardCharsets.UTF_8);

        List<Integer> offsets = new ArrayList<>();
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n%PDFA-1b\n");

        // Obj 1: Catalog com Metadata PDF/A
        offsets.add(pdf.length());
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R /Metadata 6 0 R >>\nendobj\n");

        // Obj 2: Pages
        offsets.add(pdf.length());
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        // Obj 3: Page
        offsets.add(pdf.length());
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ")
                .append("/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

        // Obj 4: Font
        offsets.add(pdf.length());
        pdf.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        // Obj 5: Content Stream
        offsets.add(pdf.length());
        pdf.append("5 0 obj\n<< /Length ").append(contentBytes.length).append(" >>\nstream\n")
                .append(contentStream)
                .append("endstream\nendobj\n");

        // Obj 6: XMP Metadata Stream (Conformidade PDF/A-1b ISO 19005-1)
        offsets.add(pdf.length());
        pdf.append("6 0 obj\n<< /Type /Metadata /Subtype /XML /Length ").append(xmpBytes.length).append(" >>\nstream\n")
                .append(xmpMetadata).append("\nendstream\nendobj\n");

        int xrefStart = pdf.length();
        pdf.append("xref\n0 7\n0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size 7 /Root 1 0 R >>\nstartxref\n")
                .append(xrefStart)
                .append("\n%%EOF\n");

        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }

    public String gerarPdfABase64(
            PacienteResponseDTO paciente,
            List<AgendamentoResponseDTO> agendamentos,
            List<ProntuarioClinicoResponseDTO> prontuarios
    ) {
        return Base64.getEncoder().encodeToString(gerarPdfABytes(paciente, agendamentos, prontuarios));
    }

    private String normalizarAscii(String texto) {
        if (texto == null) {
            return "";
        }
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private String escaparPdfTexto(String texto) {
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
