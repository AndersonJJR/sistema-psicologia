package com.anderson.sistemapsicologia.model.agendamento;

import com.anderson.sistemapsicologia.model.common.EntidadeAuditavel;
import com.anderson.sistemapsicologia.model.paciente.Paciente;
import com.anderson.sistemapsicologia.model.psicologo.Psicologo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade AGENDAMENTOS com suporte a Telessaúde (endereco_local_sessao confirmado na sessão)
 * e Soft Delete conforme Lei nº 13.787/2018.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AGENDAMENTOS")
public class Agendamento extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_agendamento", updatable = false, nullable = false)
    private UUID idAgendamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_psicologo", nullable = false)
    private Psicologo psicologo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(name = "data_hora_sessao", nullable = false)
    private LocalDateTime dataHoraSessao;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade", nullable = false, length = 25)
    private ModalidadeAtendimento modalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private StatusAgendamento status;

    @Column(name = "endereco_local_sessao", nullable = false, length = 255)
    private String enderecoLocalSessao;
}
