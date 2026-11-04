package com.anderson.sistemapsicologia.model.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    List<Pagamento> findAllByAtivoTrueOrderByDataPagamentoDesc();

    List<Pagamento> findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataPagamentoDesc(UUID idPaciente);

    Optional<Pagamento> findByIdPagamentoAndAtivoTrue(UUID idPagamento);

    boolean existsByAgendamentoIdAgendamento(UUID idAgendamento);
}
