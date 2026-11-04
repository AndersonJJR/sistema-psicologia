package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.paciente.ExportacaoPortabilidadeService;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteExportacaoDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * Endpoint de Portabilidade em Lote (/api/v1/exportacao/paciente/{id}) conforme LGPD e CFP.
 * Consolida dados estruturados em formatos abertos (JSON e CSV) com o documento PDF/A associado.
 */
@RestController
@RequestMapping("/api/v1/exportacao")
@PreAuthorize("hasAnyRole('PSICOLOGO', 'ADMIN')")
@RequiredArgsConstructor
public class ExportacaoPacienteController {

    private final ExportacaoPortabilidadeService exportacaoService;

    @GetMapping("/paciente/{id}")
    public ResponseEntity<PacienteExportacaoDTO> exportarConsolidado(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(exportacaoService.exportarDossiePaciente(id, ip));
    }

    @GetMapping(value = "/paciente/{id}/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportarCsv(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        PacienteExportacaoDTO exportacao = exportacaoService.exportarDossiePaciente(id, ip);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"paciente_" + id + "_portabilidade.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(exportacao.conteudoCsvAberto().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/paciente/{id}/pdfa", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPdfA(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        byte[] pdfBytes = exportacaoService.exportarPdfABinario(id, ip);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dossie_paciente_" + id + "_pdfa.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
