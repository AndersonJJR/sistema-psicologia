package com.anderson.sistemapsicologia.model.psicologo;

import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.RecursoNaoEncontradoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.psicologo.dtos.PsicologoRequestDTO;
import com.anderson.sistemapsicologia.model.psicologo.dtos.PsicologoResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PsicologoService {

    private final PsicologoRepository psicologoRepository;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public PsicologoResponseDTO criar(PsicologoRequestDTO dto, String enderecoIp) {
        if (psicologoRepository.existsByCpf(dto.cpf())) {
            throw new RegraNegocioException("Já existe psicólogo cadastrado com o CPF informado.");
        }
        if (psicologoRepository.existsByCrp(dto.crp())) {
            throw new RegraNegocioException("Já existe psicólogo cadastrado com o CRP informado.");
        }
        if (psicologoRepository.existsByNumeroEpsi(dto.numeroEpsi())) {
            throw new RegraNegocioException("Já existe psicólogo cadastrado com o número e-Psi informado.");
        }

        Psicologo psicologo = Psicologo.builder()
                .nome(dto.nome())
                .cpf(dto.cpf())
                .crp(dto.crp())
                .numeroEpsi(dto.numeroEpsi())
                .email(dto.email())
                .telefone(dto.telefone())
                .build();

        Psicologo salvo = psicologoRepository.save(psicologo);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.INSERCAO,
                "PSICOLOGO:" + salvo.getIdPsicologo()
        );
        return PsicologoResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<PsicologoResponseDTO> listarAtivos() {
        return psicologoRepository.findAllByAtivoTrue()
                .stream()
                .map(PsicologoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Psicologo buscarEntidadeAtiva(UUID id) {
        return psicologoRepository.findByIdPsicologoAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Psicólogo não encontrado ou inativo: " + id));
    }

    @Transactional(readOnly = true)
    public PsicologoResponseDTO buscarPorId(UUID id, String enderecoIp) {
        Psicologo psicologo = buscarEntidadeAtiva(id);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.LEITURA,
                "PSICOLOGO:" + id
        );
        return PsicologoResponseDTO.fromEntity(psicologo);
    }

    @Transactional
    public void inativar(UUID id, String enderecoIp) {
        Psicologo psicologo = buscarEntidadeAtiva(id);
        psicologo.realizarSoftDelete();
        psicologoRepository.save(psicologo);
        auditoriaService.registrarAcao(
                autenticacaoService.obterLoginOperadorAtual(),
                enderecoIp,
                AcaoAuditoria.ALTERACAO,
                "PSICOLOGO:SOFT_DELETE:" + id
        );
    }
}
