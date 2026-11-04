package com.anderson.sistemapsicologia.model.usuario.dtos;

import com.anderson.sistemapsicologia.model.usuario.PerfilUsuario;

import java.time.Instant;

public record TokenResponseDTO(
        String token,
        String tipo,
        String login,
        PerfilUsuario perfil,
        Long idPsicologoVinculado,
        Instant expiraEm
) {
}
