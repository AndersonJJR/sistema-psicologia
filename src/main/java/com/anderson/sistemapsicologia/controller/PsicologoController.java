package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.psicologo.PsicologoService;
import com.anderson.sistemapsicologia.model.psicologo.dtos.PsicologoRequestDTO;
import com.anderson.sistemapsicologia.model.psicologo.dtos.PsicologoResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/psicologos")
@RequiredArgsConstructor
public class PsicologoController {

    private final PsicologoService psicologoService;

    @PostMapping
    public ResponseEntity<PsicologoResponseDTO> criar(
            @Valid @RequestBody PsicologoRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(psicologoService.criar(dto, ip));
    }

    @GetMapping
    public ResponseEntity<List<PsicologoResponseDTO>> listar() {
        return ResponseEntity.ok(psicologoService.listarAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PsicologoResponseDTO> buscarPorId(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(psicologoService.buscarPorId(id, ip));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PsicologoResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PsicologoRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(psicologoService.atualizar(id, dto, ip));
    }

    @PatchMapping("/{id}/renovar-epsi")
    public ResponseEntity<PsicologoResponseDTO> renovarEpsi(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(psicologoService.renovarCertificadoEpsi(id, ip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        psicologoService.inativar(id, ip);
        return ResponseEntity.noContent().build();
    }
}
