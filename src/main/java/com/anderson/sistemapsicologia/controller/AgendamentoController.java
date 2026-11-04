package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.agendamento.AgendamentoService;
import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoRequestDTO;
import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> criar(
            @Valid @RequestBody AgendamentoRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.criar(dto, ip));
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDTO>> listar() {
        return ResponseEntity.ok(agendamentoService.listarAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(agendamentoService.buscarPorId(id, ip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        agendamentoService.inativar(id, ip);
        return ResponseEntity.noContent().build();
    }
}
