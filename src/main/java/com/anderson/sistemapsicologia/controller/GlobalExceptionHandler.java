package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.common.AcessoNegadoClinicoException;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.view.ErroPadraoView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroPadraoView> handleNaoEncontrado(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroPadraoView.criar(
                        HttpStatus.NOT_FOUND.value(),
                        "Recurso não encontrado",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroPadraoView> handleRegraNegocio(
            RegraNegocioException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErroPadraoView.criar(
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Violação de regra de negócio",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(AcessoNegadoClinicoException.class)
    public ResponseEntity<ErroPadraoView> handleAcessoNegadoClinico(
            AcessoNegadoClinicoException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErroPadraoView.criar(
                        HttpStatus.FORBIDDEN.value(),
                        "Acesso negado (Sigilo Profissional CFP / RBAC)",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErroPadraoView> handleOperacaoProibida(
            UnsupportedOperationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroPadraoView.criar(
                        HttpStatus.CONFLICT.value(),
                        "Operação bloqueada por conformidade legal (Lei nº 13.787/2018 / LGPD)",
                        List.of(ex.getMessage()),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoView> handleValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroPadraoView.criar(
                        HttpStatus.BAD_REQUEST.value(),
                        "Erro de validação nos dados enviados",
                        erros,
                        request.getRequestURI()
                ));
    }
}
