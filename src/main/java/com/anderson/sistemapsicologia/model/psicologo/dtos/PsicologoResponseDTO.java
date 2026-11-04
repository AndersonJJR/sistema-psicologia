package com.anderson.sistemapsicologia.model.psicologo.dtos;

import com.anderson.sistemapsicologia.model.psicologo.Psicologo;

import java.time.Instant;
import java.util.UUID;

public record PsicologoResponseDTO(
        UUID idPsicologo,
        String nome,
        String cpf,
        String crp,
        String numeroEpsi,
        String email,
        String telefone,
        boolean ativo,
        Instant deletedAt
) {
    public static PsicologoResponseDTO fromEntity(Psicologo entity) {
        return new PsicologoResponseDTO(
                entity.getIdPsicologo(),
                entity.getNome(),
                entity.getCpf(),
                entity.getCrp(),
                entity.getNumeroEpsi(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.isAtivo(),
                entity.getDeletedAt()
        );
    }
}
