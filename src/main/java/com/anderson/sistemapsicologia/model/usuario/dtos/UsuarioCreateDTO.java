package com.anderson.sistemapsicologia.model.usuario.dtos;

import com.anderson.sistemapsicologia.model.usuario.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCreateDTO(
        @NotBlank(message = "O login é obrigatório")
        String login,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String senha,
        @NotNull(message = "O perfil de acesso é obrigatório")
        PerfilUsuario perfil,
        Long idPsicologoVinculado
) {
}
