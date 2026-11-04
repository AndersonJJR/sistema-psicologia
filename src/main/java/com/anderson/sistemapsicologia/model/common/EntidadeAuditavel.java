package com.anderson.sistemapsicologia.model.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Superclasse base para entidades que exigem retenção legal de 20 anos (Lei nº 13.787/2018 e LGPD).
 * Implementa Soft Delete padronizado (DRY), impedindo a exclusão física (hard delete) de dados clínicos.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class EntidadeAuditavel {

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    /**
     * Realiza a exclusão lógica (Soft Delete) registrando o carimbo de tempo UTC.
     */
    public void realizarSoftDelete() {
        this.ativo = false;
        this.deletedAt = Instant.now();
    }

    /**
     * Reativa um registro anteriormente inativado de forma lógica.
     */
    public void reativar() {
        this.ativo = true;
        this.deletedAt = null;
    }
}
