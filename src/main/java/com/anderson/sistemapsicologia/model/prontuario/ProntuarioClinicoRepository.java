package com.anderson.sistemapsicologia.model.prontuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProntuarioClinicoRepository extends JpaRepository<ProntuarioClinico, UUID> {

    List<ProntuarioClinico> findAllByAtivoTrueOrderByDataRegistroDesc();

    List<ProntuarioClinico> findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataRegistroDesc(UUID idPaciente);

    Optional<ProntuarioClinico> findByIdProntuarioAndAtivoTrue(UUID idProntuario);

    boolean existsByAgendamentoIdAgendamento(UUID idAgendamento);

    @Override
    default void deleteById(UUID id) {
        throw new UnsupportedOperationException(
                "Exclusão física de prontuário proibida pela Lei nº 13.787/2018 (retenção de 20 anos). Utilize Soft Delete."
        );
    }

    @Override
    default void delete(ProntuarioClinico entity) {
        throw new UnsupportedOperationException(
                "Exclusão física de prontuário proibida pela Lei nº 13.787/2018 (retenção de 20 anos). Utilize Soft Delete."
        );
    }
}
