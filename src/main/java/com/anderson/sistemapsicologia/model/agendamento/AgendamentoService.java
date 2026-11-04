package com.anderson.sistemapsicologia.model.agendamento;

import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoRequestDTO;
import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoResponseDTO;
import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.paciente.Paciente;
import com.anderson.sistemapsicologia.model.paciente.PacienteService;
import com.anderson.sistemapsicologia.model.psicologo.Psicologo;
import com.anderson.sistemapsicologia.model.psicologo.PsicologoService;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PsicologoService psicologoService;
    private final PacienteService pacienteService;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public AgendamentoResponseDTO criar(AgendamentoRequestDTO dto, String enderecoIp) {
        Psicologo psicologo = psicologoService.buscarEntidadeAtiva(dto.idPsicologo());
        Paciente paciente = pacienteService.buscarEntidadeAtiva(dto.idPaciente());

        String localSessao = dto.enderecoLocalSessao() != null && !dto.enderecoLocalSessao().isBlank()
                ? dto.enderecoLocalSessao()
                : paciente.getEnderecoLocalSessao();

        if (dto.modalidade() == ModalidadeAtendimento.TELECONSULTA) {
            if (psicologo.getNumeroEpsi() == null || psicologo.getNumeroEpsi().isBlank()) {
                throw new RegraNegocioException(
                        "Atendimento online (Teleconsulta) exige psicólogo com cadastro ativo no e-Psi."
                );
            }
            if (localSessao == null || localSessao.isBlank()) {
                throw new RegraNegocioException(
                        "Teleconsulta exige o endereço físico onde o paciente se encontra para gestão de crises."
                );
            }
        }

        Agendamento agendamento = Agendamento.builder()
                .psicologo(psicologo)
                .paciente(paciente)
                .dataHoraSessao(dto.dataHoraSessao())
                .modalidade(dto.modalidade())
                .status(dto.status() != null ? dto.status() : StatusAgendamento.AGENDADO)
                .enderecoLocalSessao(localSessao)
                .build();

        Agendamento salvo = agendamentoRepository.save(agendamento);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.INSERCAO,
                "AGENDAMENTO:" + salvo.getIdAgendamento()
        );
        return AgendamentoResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarAtivos() {
        return agendamentoRepository.findAllByAtivoTrueOrderByDataHoraSessaoDesc()
                .stream()
                .map(AgendamentoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Agendamento buscarEntidadeAtiva(Long id) {
        return agendamentoRepository.findByIdAgendamentoAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado ou inativo: " + id));
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(Long id, String enderecoIp) {
        Agendamento agendamento = buscarEntidadeAtiva(id);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "AGENDAMENTO:" + id
        );
        return AgendamentoResponseDTO.fromEntity(agendamento);
    }

    @Transactional
    public void inativar(Long id, String enderecoIp) {
        Agendamento agendamento = buscarEntidadeAtiva(id);
        agendamento.realizarSoftDelete();
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "AGENDAMENTO:SOFT_DELETE:" + id
        );
    }
}
