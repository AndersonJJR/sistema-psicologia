package com.anderson.sistemapsicologia.model.upload;

import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.paciente.Paciente;
import com.anderson.sistemapsicologia.model.paciente.PacienteRepository;
import com.anderson.sistemapsicologia.model.psicologo.Psicologo;
import com.anderson.sistemapsicologia.model.psicologo.PsicologoRepository;
import com.anderson.sistemapsicologia.model.upload.dtos.UploadResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Path ROOT_UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().normalize();
    private static final List<String> EXTENSOES_PERMITIDAS = List.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final PsicologoRepository psicologoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public UploadResponseDTO salvarAvatarPsicologo(UUID idPsicologo, MultipartFile arquivo) {
        Psicologo psicologo = psicologoRepository.findById(idPsicologo)
                .orElseThrow(() -> new RegraNegocioException("Psicólogo não encontrado com o ID: " + idPsicologo));

        String nomeArquivo = salvarArquivoEmDisco("psicologos", idPsicologo.toString(), arquivo);
        String urlRelativa = "/api/v1/uploads/psicologos/" + nomeArquivo;

        psicologo.setAvatarUrl(urlRelativa);
        psicologoRepository.save(psicologo);

        usuarioRepository.findByIdPsicologoVinculado(idPsicologo).ifPresent(usuario -> {
            usuario.setAvatarUrl(urlRelativa);
            usuarioRepository.save(usuario);
        });

        log.info("Avatar atualizado para psicólogo {}: {}", idPsicologo, urlRelativa);

        return new UploadResponseDTO(
                urlRelativa,
                nomeArquivo,
                arquivo.getSize(),
                "psicologo",
                "Foto de perfil do psicólogo atualizada com sucesso."
        );
    }

    @Transactional
    public UploadResponseDTO salvarAvatarPaciente(UUID idPaciente, MultipartFile arquivo) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RegraNegocioException("Paciente não encontrado com o ID: " + idPaciente));

        String nomeArquivo = salvarArquivoEmDisco("pacientes", idPaciente.toString(), arquivo);
        String urlRelativa = "/api/v1/uploads/pacientes/" + nomeArquivo;

        paciente.setAvatarUrl(urlRelativa);
        pacienteRepository.save(paciente);

        log.info("Avatar atualizado para paciente {}: {}", idPaciente, urlRelativa);

        return new UploadResponseDTO(
                urlRelativa,
                nomeArquivo,
                arquivo.getSize(),
                "paciente",
                "Foto do paciente atualizada com sucesso."
        );
    }

    public Resource carregarArquivo(String categoria, String nomeArquivo) {
        try {
            validarCategoria(categoria);
            Path diretorio = ROOT_UPLOAD_DIR.resolve(categoria).normalize();
            Path caminhoArquivo = diretorio.resolve(nomeArquivo).normalize();

            if (!caminhoArquivo.startsWith(ROOT_UPLOAD_DIR)) {
                throw new RegraNegocioException("Tentativa de acesso a caminho inválido de arquivo.");
            }

            Resource resource = new UrlResource(caminhoArquivo.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RegraNegocioException("Arquivo não encontrado: " + nomeArquivo);
            }
        } catch (MalformedURLException e) {
            throw new RegraNegocioException("Erro ao construir URL do arquivo solicitado.");
        }
    }

    private String salvarArquivoEmDisco(String categoria, String idReferencia, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("Nenhum arquivo de imagem foi enviado.");
        }

        if (arquivo.getSize() > MAX_FILE_SIZE) {
            throw new RegraNegocioException("O tamanho máximo do arquivo de imagem permitido é 10MB.");
        }

        String nomeOriginal = StringUtils.cleanPath(arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "imagem.png");
        String extensao = extrairExtensao(nomeOriginal);

        if (!EXTENSOES_PERMITIDAS.contains(extensao.toLowerCase())) {
            throw new RegraNegocioException("Formato de arquivo não suportado. Utilize JPG, PNG, WEBP ou GIF.");
        }

        try {
            Path diretorioDestino = ROOT_UPLOAD_DIR.resolve(categoria).normalize();
            if (!Files.exists(diretorioDestino)) {
                Files.createDirectories(diretorioDestino);
            }

            String sufixoUnico = UUID.randomUUID().toString().substring(0, 8);
            String nomeFinal = String.format("%s_%d_%s%s", idReferencia, System.currentTimeMillis(), sufixoUnico, extensao);
            Path arquivoDestino = diretorioDestino.resolve(nomeFinal).normalize();

            try (InputStream inputStream = arquivo.getInputStream()) {
                Files.copy(inputStream, arquivoDestino, StandardCopyOption.REPLACE_EXISTING);
            }

            return nomeFinal;
        } catch (IOException e) {
            log.error("Falha ao salvar arquivo no diretório uploads/{}: {}", categoria, e.getMessage(), e);
            throw new RegraNegocioException("Falha ao gravar arquivo de imagem no sistema: " + e.getMessage());
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        int ponto = nomeArquivo.lastIndexOf('.');
        if (ponto >= 0 && ponto < nomeArquivo.length() - 1) {
            return nomeArquivo.substring(ponto).toLowerCase();
        }
        return ".png";
    }

    private void validarCategoria(String categoria) {
        if (!"psicologos".equalsIgnoreCase(categoria) && !"pacientes".equalsIgnoreCase(categoria)) {
            throw new RegraNegocioException("Categoria de upload inválida.");
        }
    }
}
