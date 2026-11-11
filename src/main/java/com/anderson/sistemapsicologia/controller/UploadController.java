package com.anderson.sistemapsicologia.controller;

import com.anderson.sistemapsicologia.model.upload.UploadService;
import com.anderson.sistemapsicologia.model.upload.dtos.UploadResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/psicologo/{id}")
    public ResponseEntity<UploadResponseDTO> uploadAvatarPsicologo(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        UploadResponseDTO response = uploadService.salvarAvatarPsicologo(id, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/paciente/{id}")
    public ResponseEntity<UploadResponseDTO> uploadAvatarPaciente(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        UploadResponseDTO response = uploadService.salvarAvatarPaciente(id, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoria}/{nomeArquivo:.+}")
    public ResponseEntity<Resource> exibirArquivo(
            @PathVariable String categoria,
            @PathVariable String nomeArquivo
    ) {
        Resource resource = uploadService.carregarArquivo(categoria, nomeArquivo);
        MediaType mediaType = MediaTypeFactory.getMediaType(nomeArquivo)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, public")
                .body(resource);
    }
}
