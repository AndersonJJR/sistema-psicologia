package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.dashboard.DashboardService;
import com.anderson.sistemapsicologia.model.dashboard.dtos.DashboardResumoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoResponseDTO> obterResumo() {
        return ResponseEntity.ok(dashboardService.obterResumo());
    }
}
