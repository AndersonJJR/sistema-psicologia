package com.anderson.sistemapsicologia.model.psicologo;

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

/**
 * Entidade PSICOLOGOS com constraints UNIQUE em cpf, crp e numero_epsi (Telessaúde CFP).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "PSICOLOGOS",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_psicologos_cpf", columnNames = "cpf"),
                @UniqueConstraint(name = "uk_psicologos_crp", columnNames = "crp"),
                @UniqueConstraint(name = "uk_psicologos_numero_epsi", columnNames = "numero_epsi")
        }
)
public class Psicologo extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_psicologo")
    private Long idPsicologo;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "cpf", nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "crp", nullable = false, unique = true, length = 25)
    private String crp;

    @Column(name = "numero_epsi", nullable = false, unique = true, length = 40)
    private String numeroEpsi;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telefone", nullable = false, length = 25)
    private String telefone;
}
