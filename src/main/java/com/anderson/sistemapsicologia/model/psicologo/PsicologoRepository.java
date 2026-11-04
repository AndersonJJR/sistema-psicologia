package com.anderson.sistemapsicologia.model.psicologo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PsicologoRepository extends JpaRepository<Psicologo, UUID> {

    List<Psicologo> findAllByAtivoTrue();

    Optional<Psicologo> findByIdPsicologoAndAtivoTrue(UUID idPsicologo);

    boolean existsByCpf(String cpf);

    boolean existsByCrp(String crp);

    boolean existsByNumeroEpsi(String numeroEpsi);
}
