package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import com.anderson.sistemapsicologia.model.usuario.dtos.LoginRequestDTO;
import com.anderson.sistemapsicologia.model.usuario.dtos.RegistroPsicologoAuthRequestDTO;
import com.anderson.sistemapsicologia.model.usuario.dtos.TokenResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.dtos.UsuarioCreateDTO;
import com.anderson.sistemapsicologia.model.usuario.dtos.UsuarioResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.ok(autenticacaoService.autenticar(dto, ip));
    }

    @PostMapping("/registrar-psicologo")
    public ResponseEntity<TokenResponseDTO> registrarPsicologo(
            @Valid @RequestBody RegistroPsicologoAuthRequestDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(autenticacaoService.registrarPsicologo(dto, ip));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(
            @Valid @RequestBody UsuarioCreateDTO dto,
            HttpServletRequest request
    ) {
        String ip = RequisicaoHttpUtils.extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(autenticacaoService.cadastrarUsuario(dto, ip));
    }
}
