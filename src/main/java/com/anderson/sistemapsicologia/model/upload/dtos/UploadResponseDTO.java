package com.anderson.sistemapsicologia.model.upload.dtos;

public record UploadResponseDTO(
        String url,
        String nomeArquivo,
        long tamanho,
        String tipo,
        String mensagem
) {
}
