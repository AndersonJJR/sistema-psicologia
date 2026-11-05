package com.anderson.sistemapsicologia.model.pagamento;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.common.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidade PAGAMENTOS estruturada com Split de Pagamentos para evitar bitributação:
 * segrega valor_total em taxa_clinica_infraestrutura (base NFS-e da clínica) e
 * repasse_profissional (base Carnê-Leão/NFS-e do psicólogo).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PAGAMENTOS")
public class Pagamento extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pagamento", updatable = false, nullable = false)
    private UUID idPagamento;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_agendamento", nullable = false, unique = true)
    private Agendamento agendamento;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "taxa_clinica_infraestrutura", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxaClinicaInfraestrutura;

    @Column(name = "repasse_profissional", nullable = false, precision = 12, scale = 2)
    private BigDecimal repasseProfissional;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_split", nullable = false, length = 25)
    private StatusSplit statusSplit;

    @Builder.Default
    @Column(name = "meio_pagamento", nullable = false, length = 25, columnDefinition = "varchar(25) default 'PIX'")
    private String meioPagamento = "PIX";

    @Column(name = "data_pagamento", nullable = false)
    private Instant dataPagamento;

    @PrePersist
    protected void prePersistPagamento() {
        super.onCreate();
        if (this.dataPagamento == null) {
            this.dataPagamento = Instant.now();
        }
        if (this.statusSplit == null) {
            this.statusSplit = StatusSplit.PENDENTE;
        }
        if (this.meioPagamento == null || this.meioPagamento.isBlank()) {
            this.meioPagamento = "PIX";
        }
    }
}
