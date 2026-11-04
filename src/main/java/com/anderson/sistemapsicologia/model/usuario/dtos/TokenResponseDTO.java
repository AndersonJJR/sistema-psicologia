package com.anderson.sistemapsicologia.model.usuario.dtos;

import com.anderson.sistemapsicologia.model.usuario.PerfilUsuario;

import java.time.Instant;
import java.util.UUID;

public record TokenResponseDTO(
        String token,
        String tipo,
        String login,
        PerfilUsuario perfil,
        UUID idPsicologoVinculado,
        Instant expiraEm
) {
}
