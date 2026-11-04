package com.anderson.sistemapsicologia.model.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    List<LogAuditoria> findByIdRecursoAcessadoContainingOrderByCarimboTempoUtcDesc(String idRecursoAcessado);

    List<LogAuditoria> findAllByOrderByCarimboTempoUtcDesc();

    @Override
    default void deleteById(Long id) {
        throw new UnsupportedOperationException("LOGS_AUDITORIA é append-only: exclusões não são permitidas.");
    }

    @Override
    default void delete(LogAuditoria entity) {
        throw new UnsupportedOperationException("LOGS_AUDITORIA é append-only: exclusões não são permitidas.");
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException("LOGS_AUDITORIA é append-only: exclusões não são permitidas.");
    }
}
