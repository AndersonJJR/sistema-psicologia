package com.anderson.sistemapsicologia.model.paciente;

import com.anderson.sistemapsicologia.model.common.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade PACIENTES com salvaguardas operacionais para telessaúde e gestão de crises:
 * contato_emergencia_nome, contato_emergencia_telefone e endereco_local_sessao.
 * Implementa Soft Delete (ativo / deleted_at) conforme Lei nº 13.787/2018 (retenção de 20 anos).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "PACIENTES",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pacientes_cpf", columnNames = "cpf")
        }
)
public class Paciente extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_paciente", updatable = false, nullable = false)
    private UUID idPaciente;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "cpf", nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telefone", nullable = false, length = 25)
    private String telefone;

    @Column(name = "contato_emergencia_nome", nullable = false, length = 150)
    private String contatoEmergenciaNome;

    @Column(name = "contato_emergencia_telefone", nullable = false, length = 25)
    private String contatoEmergenciaTelefone;

    @Column(name = "endereco_local_sessao", nullable = false, length = 255)
    private String enderecoLocalSessao;
}
