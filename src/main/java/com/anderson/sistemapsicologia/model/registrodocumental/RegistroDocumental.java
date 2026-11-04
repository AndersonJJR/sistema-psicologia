package com.anderson.sistemapsicologia.model.registrodocumental;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.common.AesAttributeConverter;
import com.anderson.sistemapsicologia.model.common.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
 * Entidade REGISTROS_DOCUMENTAIS conforme Resolução CFP nº 001/2009.
 * Segrega anotações privativas do psicólogo titular (hipóteses provisórias e notas de supervisão),
 * criptografadas em nível de aplicação (ALE - AES-256-GCM) antes da persistência no MySQL.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "REGISTROS_DOCUMENTAIS")
public class RegistroDocumental extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_registro_documental", updatable = false, nullable = false)
    private UUID idRegistroDocumental;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_agendamento", nullable = false, unique = true)
    private Agendamento agendamento;

    @Convert(converter = AesAttributeConverter.class)
    @Column(name = "hipoteses_provisorias", nullable = false, columnDefinition = "TEXT")
    private String hipotesesProvisorias;

    @Convert(converter = AesAttributeConverter.class)
    @Column(name = "notas_supervisao", nullable = false, columnDefinition = "TEXT")
    private String notasSupervisao;

    @Column(name = "data_registro", nullable = false)
    private Instant dataRegistro;

    @Builder.Default
    @Column(name = "assinado", nullable = false, columnDefinition = "boolean default false")
    private boolean assinado = false;

    @PrePersist
    protected void prePersistRegistro() {
        super.onCreate();
        if (this.dataRegistro == null) {
            this.dataRegistro = Instant.now();
        }
    }
}
