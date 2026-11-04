package com.anderson.sistemapsicologia.model.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findAllByAtivoTrueOrderByDataPagamentoDesc();

    List<Pagamento> findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataPagamentoDesc(Long idPaciente);

    Optional<Pagamento> findByIdPagamentoAndAtivoTrue(Long idPagamento);

    boolean existsByAgendamentoIdAgendamento(Long idAgendamento);
}
