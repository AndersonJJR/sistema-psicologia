package com.anderson.sistemapsicologia.model.usuario.dtos;

import com.anderson.sistemapsicologia.model.usuario.PerfilUsuario;
import com.anderson.sistemapsicologia.model.usuario.Usuario;

public record UsuarioResponseDTO(
        Long idUsuario,
        String login,
        PerfilUsuario perfil,
        Long idPsicologoVinculado,
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
