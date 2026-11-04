package com.anderson.sistemapsicologia.model.paciente;

import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteRequestDTO;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public PacienteResponseDTO criar(PacienteRequestDTO dto, String enderecoIp) {
        if (pacienteRepository.existsByCpf(dto.cpf())) {
            throw new RegraNegocioException("Já existe paciente cadastrado com o CPF informado.");
        }

        Paciente paciente = Paciente.builder()
                .nome(dto.nome())
                .cpf(dto.cpf())
                .dataNascimento(dto.dataNascimento())
                .email(dto.email())
                .telefone(dto.telefone())
                .contatoEmergenciaNome(dto.contatoEmergenciaNome())
                .contatoEmergenciaTelefone(dto.contatoEmergenciaTelefone())
                .enderecoLocalSessao(dto.enderecoLocalSessao())
                .build();

        Paciente salvo = pacienteRepository.save(paciente);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.INSERCAO,
                "PACIENTE:" + salvo.getIdPaciente()
        );
        return PacienteResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarAtivos() {
        return pacienteRepository.findAllByAtivoTrue()
                .stream()
                .map(PacienteResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarTodos() {
        return pacienteRepository.findAll()
                .stream()
                .map(PacienteResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Paciente buscarEntidadePorId(UUID id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Paciente buscarEntidadeAtiva(UUID id) {
        return pacienteRepository.findByIdPacienteAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado ou inativo: " + id));
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorId(UUID id, String enderecoIp) {
        Paciente paciente = buscarEntidadeAtiva(id);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "PACIENTE:" + id
        );
        return PacienteResponseDTO.fromEntity(paciente);
    }

    @Transactional
    public PacienteResponseDTO atualizar(UUID id, PacienteRequestDTO dto, String enderecoIp) {
        Paciente paciente = buscarEntidadePorId(id);
        if (!paciente.getCpf().equals(dto.cpf()) && pacienteRepository.existsByCpf(dto.cpf())) {
            throw new RegraNegocioException("Já existe outro paciente cadastrado com o CPF informado.");
        }
        paciente.setNome(dto.nome());
        paciente.setCpf(dto.cpf());
        paciente.setDataNascimento(dto.dataNascimento());
        paciente.setEmail(dto.email());
        paciente.setTelefone(dto.telefone());
        paciente.setContatoEmergenciaNome(dto.contatoEmergenciaNome());
        paciente.setContatoEmergenciaTelefone(dto.contatoEmergenciaTelefone());
        paciente.setEnderecoLocalSessao(dto.enderecoLocalSessao());

        Paciente salvo = pacienteRepository.save(paciente);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "PACIENTE:" + id
        );
        return PacienteResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public PacienteResponseDTO alterarStatusAtivo(UUID id, boolean ativo, String enderecoIp) {
        Paciente paciente = buscarEntidadePorId(id);
        if (ativo) {
            paciente.reativar();
        } else {
            paciente.realizarSoftDelete();
        }
        Paciente salvo = pacienteRepository.save(paciente);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "PACIENTE:STATUS_" + (ativo ? "ATIVO:" : "INATIVO:") + id
        );
        return PacienteResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public void inativar(UUID id, String enderecoIp) {
        Paciente paciente = buscarEntidadeAtiva(id);
        paciente.realizarSoftDelete();
        pacienteRepository.save(paciente);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "PACIENTE:SOFT_DELETE:" + id
        );
    }
}
