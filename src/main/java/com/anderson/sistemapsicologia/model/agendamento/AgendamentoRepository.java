package com.anderson.sistemapsicologia.model.agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findAllByAtivoTrueOrderByDataHoraSessaoDesc();

    List<Agendamento> findAllByPacienteIdPacienteOrderByDataHoraSessaoDesc(Long idPaciente);

    Optional<Agendamento> findByIdAgendamentoAndAtivoTrue(Long idAgendamento);

    @Override
    default void deleteById(Long id) {
        throw new UnsupportedOperationException(
                "Exclusão física (hard delete) proibida pela Lei nº 13.787/2018. Utilize Soft Delete."
        );
    }

    @Override
    default void delete(Agendamento entity) {
        throw new UnsupportedOperationException(
                "Exclusão física (hard delete) proibida pela Lei nº 13.787/2018. Utilize Soft Delete."
        );
    }
}
