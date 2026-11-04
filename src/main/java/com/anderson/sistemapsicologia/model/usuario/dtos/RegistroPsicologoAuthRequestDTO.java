package com.anderson.sistemapsicologia.model.usuario.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroPsicologoAuthRequestDTO(
        @NotBlank(message = "O nome completo do psicólogo é obrigatório")
        String nome,
        @NotBlank(message = "O e-mail profissional é obrigatório")
        @Email(message = "Informe um e-mail profissional válido")
        String email,
        @NotBlank(message = "O registro CRP é obrigatório")
        String crp,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String senha,
        String cpf,
        String telefone,
        String numeroEpsi
) {
}
