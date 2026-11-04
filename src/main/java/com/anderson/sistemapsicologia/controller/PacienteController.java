package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.paciente.PacienteService;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteRequestDTO;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> criar(
            @Valid @RequestBody PacienteRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.criar(dto, ip));
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> listar() {
        return ResponseEntity.ok(pacienteService.listarAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> buscarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(pacienteService.buscarPorId(id, ip));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PacienteRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(pacienteService.atualizar(id, dto, ip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        pacienteService.inativar(id, ip);
        return ResponseEntity.noContent().build();
    }
}
