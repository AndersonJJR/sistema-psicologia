package com.anderson.sistemapsicologia.model.paciente.dtos;

import com.anderson.sistemapsicologia.model.paciente.Paciente;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PacienteResponseDTO(
        UUID idPaciente,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String enderecoLocalSessao,
        boolean ativo,
        Instant deletedAt
) {
    public static PacienteResponseDTO fromEntity(Paciente entity) {
        return new PacienteResponseDTO(
                entity.getIdPaciente(),
                entity.getNome(),
                entity.getCpf(),
                entity.getDataNascimento(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getContatoEmergenciaNome(),
                entity.getContatoEmergenciaTelefone(),
                entity.getEnderecoLocalSessao(),
                entity.isAtivo(),
                entity.getDeletedAt()
        );
    }
}
