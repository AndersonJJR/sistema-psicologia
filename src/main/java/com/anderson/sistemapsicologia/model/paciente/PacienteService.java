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
    public Paciente buscarEntidadePorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Paciente buscarEntidadeAtiva(Long id) {
        return pacienteRepository.findByIdPacienteAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado ou inativo: " + id));
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorId(Long id, String enderecoIp) {
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
    public PacienteResponseDTO atualizar(Long id, PacienteRequestDTO dto, String enderecoIp) {
        Paciente paciente = buscarEntidadeAtiva(id);
        paciente.setNome(dto.nome());
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
    public void inativar(Long id, String enderecoIp) {
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
