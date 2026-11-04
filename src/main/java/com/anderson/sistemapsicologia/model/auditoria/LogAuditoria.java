package com.anderson.sistemapsicologia.model.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/**
 * Entidade de auditoria no modelo append-only (apenas inserções, sem atualizações ou exclusões).
 * Atende às exigências de rastreabilidade da LGPD e retenção da Lei nº 13.787/2018.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Entity
@Table(name = "LOGS_AUDITORIA")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log", updatable = false, nullable = false)
    private Long idLog;

    @Column(name = "carimbo_tempo_utc", updatable = false, nullable = false)
    private Instant carimboTempoUtc;

    @Column(name = "id_usuario_operador", updatable = false, nullable = false, length = 120)
    private String idUsuarioOperador;

    @Column(name = "endereco_ip", updatable = false, nullable = false, length = 64)
    private String enderecoIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao", updatable = false, nullable = false, length = 30)
    private AcaoAuditoria acao;

    @Column(name = "id_recurso_acessado", updatable = false, nullable = false, length = 150)
    private String idRecursoAcessado;

    @PrePersist
    protected void prePersist() {
        if (this.carimboTempoUtc == null) {
            this.carimboTempoUtc = Instant.now();
        }
    }

    @PreUpdate
    protected void preventUpdate() {
        throw new UnsupportedOperationException("LOGS_AUDITORIA é append-only: atualizações não são permitidas.");
    }

    @PreRemove
    protected void preventRemove() {
        throw new UnsupportedOperationException("LOGS_AUDITORIA é append-only: exclusões não são permitidas.");
    }
}
