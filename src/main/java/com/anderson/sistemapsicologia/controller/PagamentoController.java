package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.pagamento.PagamentoService;
import com.anderson.sistemapsicologia.model.pagamento.dtos.PagamentoRequestDTO;
import com.anderson.sistemapsicologia.model.pagamento.dtos.PagamentoResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> registrar(
            @Valid @RequestBody PagamentoRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamentoService.registrarPagamento(dto, ip));
    }

    @PatchMapping("/{id}/liquidar")
    public ResponseEntity<PagamentoResponseDTO> liquidarSplit(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(pagamentoService.liquidarSplit(id, ip));
    }

    @GetMapping
    public ResponseEntity<List<PagamentoResponseDTO>> listar() {
        return ResponseEntity.ok(pagamentoService.listarAtivos());
    }

    @GetMapping(value = "/carne-leao/csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportarCarneLeaoCsv(HttpServletRequest request) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        String csv = pagamentoService.gerarCsvCarneLeao(ip);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"carne-leao-livro-caixa.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/comprovante-xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> exportarComprovanteXml(
            @RequestParam(required = false) UUID idPagamento,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        String xml = pagamentoService.gerarXmlSplitPagamento(idPagamento, ip);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprovante-split-pagamento.xml\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml.getBytes(StandardCharsets.UTF_8));
    }
}
