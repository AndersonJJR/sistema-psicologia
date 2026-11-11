package com.anderson.sistemapsicologia.model.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByLoginAndAtivoTrue(String login);

    Optional<Usuario> findByIdPsicologoVinculado(UUID idPsicologoVinculado);

    boolean existsByLogin(String login);
}
