package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinicoService;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoRequestDTO;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoResponseDTO;
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
import java.util.UUID;

/**
 * Controller REST para PRONTUARIOS_CLINICOS (Resolução CFP nº 001/2009).
 * Bloqueia acesso do perfil RECEPCAO via RBAC.
 */
@RestController
@RequestMapping("/api/v1/prontuarios")
@PreAuthorize("hasAnyRole('PSICOLOGO', 'ADMIN')")
@RequiredArgsConstructor
public class ProntuarioClinicoController {

    private final ProntuarioClinicoService prontuarioService;

    @PostMapping
    public ResponseEntity<ProntuarioClinicoResponseDTO> criar(
            @Valid @RequestBody ProntuarioClinicoRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(prontuarioService.criar(dto, ip));
    }

    @GetMapping
    public ResponseEntity<List<ProntuarioClinicoResponseDTO>> listar(HttpServletRequest request) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(prontuarioService.listarAtivos(ip));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProntuarioClinicoResponseDTO> buscarPorId(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(prontuarioService.buscarPorId(id, ip));
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<ProntuarioClinicoResponseDTO>> listarPorPaciente(
            @PathVariable UUID idPaciente,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(prontuarioService.listarPorPaciente(idPaciente, ip));
    }

    @GetMapping("/agendamento/{idAgendamento}")
    public ResponseEntity<ProntuarioClinicoResponseDTO> buscarPorAgendamento(
            @PathVariable UUID idAgendamento,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(prontuarioService.buscarPorAgendamento(idAgendamento, ip));
    }

    @PostMapping("/agendamento/{idAgendamento}/assinar")
    public ResponseEntity<ProntuarioClinicoResponseDTO> assinarPorAgendamento(
            @PathVariable UUID idAgendamento,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(prontuarioService.assinarPorAgendamento(idAgendamento, ip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        prontuarioService.inativar(id, ip);
        return ResponseEntity.noContent().build();
    }
}
