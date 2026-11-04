package com.anderson.sistemapsicologia.model.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findAllByAtivoTrue();

    Optional<Paciente> findByIdPacienteAndAtivoTrue(Long idPaciente);

    boolean existsByCpf(String cpf);

    @Override
    default void deleteById(Long id) {
        throw new UnsupportedOperationException(
                "Exclusão física (hard delete) proibida pela Lei nº 13.787/2018. Utilize Soft Delete."
        );
    }

    @Override
    default void delete(Paciente entity) {
        throw new UnsupportedOperationException(
                "Exclusão física (hard delete) proibida pela Lei nº 13.787/2018. Utilize Soft Delete."
        );
    }
}
