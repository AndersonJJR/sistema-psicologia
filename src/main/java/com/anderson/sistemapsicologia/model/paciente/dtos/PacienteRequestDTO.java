package com.anderson.sistemapsicologia.model.paciente.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PacienteRequestDTO(
        @NotBlank(message = "O nome do paciente é obrigatório")
        String nome,
        @NotBlank(message = "O CPF do paciente é obrigatório")
        String cpf,
        @NotNull(message = "A data de nascimento é obrigatória")
        LocalDate dataNascimento,
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,
        @NotBlank(message = "O telefone é obrigatório")
        String telefone,
        @NotBlank(message = "O nome do contato de emergência é obrigatório")
        String contatoEmergenciaNome,
        @NotBlank(message = "O telefone do contato de emergência é obrigatório")
        String contatoEmergenciaTelefone,
        @NotBlank(message = "O endereço do local da sessão é obrigatório para suporte a crises em telessaúde")
        String enderecoLocalSessao
) {
}
