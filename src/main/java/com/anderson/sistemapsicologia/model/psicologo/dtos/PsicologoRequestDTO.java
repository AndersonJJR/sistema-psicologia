package com.anderson.sistemapsicologia.model.psicologo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PsicologoRequestDTO(
        @NotBlank(message = "O nome do psicólogo é obrigatório")
        String nome,
        @NotBlank(message = "O CPF do psicólogo é obrigatório")
        String cpf,
        @NotBlank(message = "O CRP do psicólogo é obrigatório")
        String crp,
        @NotBlank(message = "O número de cadastro e-Psi é obrigatório para telessaúde")
        String numeroEpsi,
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,
        @NotBlank(message = "O telefone é obrigatório")
        String telefone,
        String abordagemClinica
) {
}
