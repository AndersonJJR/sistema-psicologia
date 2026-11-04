package com.anderson.sistemapsicologia.model.auditoria;

import com.anderson.sistemapsicologia.model.auditoria.dtos.LogAuditoriaCreateDTO;
import com.anderson.sistemapsicologia.model.auditoria.dtos.LogAuditoriaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;

    @Transactional
    public LogAuditoriaResponseDTO registrarEvento(LogAuditoriaCreateDTO dto) {
        LogAuditoria log = LogAuditoria.builder()
                .carimboTempoUtc(Instant.now())
                .idUsuarioOperador(dto.idUsuarioOperador())
                .enderecoIp(dto.enderecoIp())
                .acao(dto.acao())
                .idRecursoAcessado(dto.idRecursoAcessado())
                .build();
        return LogAuditoriaResponseDTO.fromEntity(logAuditoriaRepository.save(log));
    }

    @Transactional
    public LogAuditoriaResponseDTO registrarAcao(String operador, String ip, AcaoAuditoria acao, String recurso) {
        return registrarEvento(new LogAuditoriaCreateDTO(
                operador != null && !operador.isBlank() ? operador : "ANONIMO",
                ip != null && !ip.isBlank() ? ip : "0.0.0.0",
                acao,
                recurso
        ));
    }

    @Transactional(readOnly = true)
    public List<LogAuditoriaResponseDTO> listarTodos() {
        return logAuditoriaRepository.findAllByOrderByCarimboTempoUtcDesc()
                .stream()
                .map(LogAuditoriaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LogAuditoriaResponseDTO> buscarPorRecurso(String idRecurso) {
        return logAuditoriaRepository.findByIdRecursoAcessadoContainingOrderByCarimboTempoUtcDesc(idRecurso)
                .stream()
                .map(LogAuditoriaResponseDTO::fromEntity)
                .toList();
    }
}
