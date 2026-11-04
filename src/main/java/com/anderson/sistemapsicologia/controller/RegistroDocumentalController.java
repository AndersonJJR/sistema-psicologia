package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumentalService;
import com.anderson.sistemapsicologia.model.registrodocumental.dtos.RegistroDocumentalRequestDTO;
import com.anderson.sistemapsicologia.model.registrodocumental.dtos.RegistroDocumentalResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para REGISTROS_DOCUMENTAIS (Resolução CFP nº 001/2009).
 * Exclusivo para perfil PSICOLOGO e filtrado estritamente pelo psicólogo titular (via JWT).
 */
@RestController
@RequestMapping("/api/v1/registros-documentais")
@PreAuthorize("hasRole('PSICOLOGO')")
@RequiredArgsConstructor
public class RegistroDocumentalController {

    private final RegistroDocumentalService registroDocumentalService;

    @PostMapping
    public ResponseEntity<RegistroDocumentalResponseDTO> criar(
            @Valid @RequestBody RegistroDocumentalRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registroDocumentalService.criar(dto, ip));
    }

    @GetMapping
    public ResponseEntity<List<RegistroDocumentalResponseDTO>> listarMeusRegistros(HttpServletRequest request) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(registroDocumentalService.listarMeusRegistros(ip));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroDocumentalResponseDTO> buscarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(registroDocumentalService.buscarPorId(id, ip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        registroDocumentalService.inativar(id, ip);
        return ResponseEntity.noContent().build();
    }
}
