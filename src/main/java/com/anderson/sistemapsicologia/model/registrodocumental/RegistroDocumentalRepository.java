package com.anderson.sistemapsicologia.model.registrodocumental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroDocumentalRepository extends JpaRepository<RegistroDocumental, Long> {

    /**
     * Filtra registros documentais exclusivamente pelo psicólogo titular (identificado via JWT).
     */
    List<RegistroDocumental> findAllByAgendamentoPsicologoIdPsicologoAndAtivoTrueOrderByDataRegistroDesc(Long idPsicologoTitular);

    Optional<RegistroDocumental> findByIdRegistroDocumentalAndAtivoTrue(Long idRegistroDocumental);

    boolean existsByAgendamentoIdAgendamento(Long idAgendamento);

    @Override
    default void deleteById(Long id) {
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
