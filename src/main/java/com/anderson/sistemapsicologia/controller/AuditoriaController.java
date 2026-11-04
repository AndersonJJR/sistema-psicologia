package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.auditoria.dtos.LogAuditoriaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
@PreAuthorize("hasAnyRole('PSICOLOGO', 'ADMIN')")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<LogAuditoriaResponseDTO>> listar(
            @RequestParam(required = false) String recurso
    ) {
        if (recurso != null && !recurso.isBlank()) {
            return ResponseEntity.ok(auditoriaService.buscarPorRecurso(recurso));
        }
        return ResponseEntity.ok(auditoriaService.listarTodos());
    }
}
