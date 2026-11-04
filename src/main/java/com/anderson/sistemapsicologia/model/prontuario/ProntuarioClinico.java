package com.anderson.sistemapsicologia.model.prontuario;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.common.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade PRONTUARIOS_CLINICOS conforme Resolução CFP nº 001/2009.
 * Armazena procedimentos técnicos e evolução observável acessíveis ao paciente.
 * Suporta Soft Delete (retenção obrigatória de 20 anos pela Lei nº 13.787/2018).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PRONTUARIOS_CLINICOS")
public class ProntuarioClinico extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_prontuario", updatable = false, nullable = false)
    private UUID idProntuario;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_agendamento", nullable = false, unique = true)
    private Agendamento agendamento;

    @Column(name = "procedimentos_tecnicos", nullable = false, columnDefinition = "TEXT")
    private String procedimentosTecnicos;

    @Column(name = "evolucao_observavel", nullable = false, columnDefinition = "TEXT")
    private String evolucaoObservavel;

    @Column(name = "data_registro", nullable = false)
    private Instant dataRegistro;

    @Builder.Default
    @Column(name = "assinado", nullable = false, columnDefinition = "boolean default false")
    private boolean assinado = false;

    @Column(name = "hash_assinatura", length = 128)
    private String hashAssinatura;

    @PrePersist
    protected void prePersistProntuario() {
        super.onCreate();
        if (this.dataRegistro == null) {
            this.dataRegistro = Instant.now();
        }
    }
}
