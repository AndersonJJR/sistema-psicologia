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
        String abordagemClinica,
        Instant ultimaRenovacaoEpsi,
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
                entity.getAbordagemClinica() != null ? entity.getAbordagemClinica() : "tcc",
                entity.getUltimaRenovacaoEpsi(),
                entity.isAtivo(),
                entity.getDeletedAt()
        );
    }
}
