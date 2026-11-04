package com.anderson.sistemapsicologia.model.prontuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProntuarioClinicoRepository extends JpaRepository<ProntuarioClinico, Long> {

    List<ProntuarioClinico> findAllByAtivoTrueOrderByDataRegistroDesc();

    List<ProntuarioClinico> findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataRegistroDesc(Long idPaciente);

    Optional<ProntuarioClinico> findByIdProntuarioAndAtivoTrue(Long idProntuario);

    boolean existsByAgendamentoIdAgendamento(Long idAgendamento);

    @Override
    default void deleteById(Long id) {
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
