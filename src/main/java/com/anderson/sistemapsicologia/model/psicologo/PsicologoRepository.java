package com.anderson.sistemapsicologia.model.psicologo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsicologoRepository extends JpaRepository<Psicologo, Long> {

    List<Psicologo> findAllByAtivoTrue();

    Optional<Psicologo> findByIdPsicologoAndAtivoTrue(Long idPsicologo);

    boolean existsByCpf(String cpf);

    boolean existsByCrp(String crp);

    boolean existsByNumeroEpsi(String numeroEpsi);
}
