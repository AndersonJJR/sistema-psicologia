package com.anderson.sistemapsicologia.model.registrodocumental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistroDocumentalRepository extends JpaRepository<RegistroDocumental, UUID> {

    /**
     * Filtra registros documentais exclusivamente pelo psicólogo titular (identificado via JWT).
     */
    List<RegistroDocumental> findAllByAgendamentoPsicologoIdPsicologoAndAtivoTrueOrderByDataRegistroDesc(UUID idPsicologoTitular);

    Optional<RegistroDocumental> findByIdRegistroDocumentalAndAtivoTrue(UUID idRegistroDocumental);

    Optional<RegistroDocumental> findByAgendamentoIdAgendamentoAndAtivoTrue(UUID idAgendamento);

    boolean existsByAgendamentoIdAgendamento(UUID idAgendamento);

    @Override
    default void deleteById(UUID id) {
        throw new UnsupportedOperationException(
                "Exclusão física de registro documental proibida pela Lei nº 13.787/2018. Utilize Soft Delete."
        );
    }

    @Override
    default void delete(RegistroDocumental entity) {
        throw new UnsupportedOperationException(
                "Exclusão física de registro documental proibida pela Lei nº 13.787/2018. Utilize Soft Delete."
        );
    }
}
