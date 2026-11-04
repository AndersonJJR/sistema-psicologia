package com.anderson.sistemapsicologia.model.usuario.dtos;

import com.anderson.sistemapsicologia.model.usuario.PerfilUsuario;
import com.anderson.sistemapsicologia.model.usuario.Usuario;

import java.util.UUID;

public record UsuarioResponseDTO(
        UUID idUsuario,
        String login,
        PerfilUsuario perfil,
        UUID idPsicologoVinculado,
        boolean ativo
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getLogin(),
                usuario.getPerfil(),
                usuario.getIdPsicologoVinculado(),
                usuario.isAtivo()
        );
    }
}
