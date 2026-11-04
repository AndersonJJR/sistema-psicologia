package com.anderson.sistemapsicologia.model.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Conversor JPA que intercepta a gravação e leitura no banco MySQL para aplicar
 * Criptografia no Nível da Aplicação (ALE - AES-256-GCM).
 */
@Converter
public class AesAttributeConverter implements AttributeConverter<String, String> {

    private final CriptografiaAesService criptografiaAesService = new CriptografiaAesService(
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
    );

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return criptografiaAesService.criptografar(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return criptografiaAesService.descriptografar(dbData);
    }
}
