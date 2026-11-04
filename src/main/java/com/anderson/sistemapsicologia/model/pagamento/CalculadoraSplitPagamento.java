package com.anderson.sistemapsicologia.model.pagamento;

import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Componente de domínio (SRP/SOLID) responsável pela validação e cálculo do Split de Pagamentos.
 */
@Component
public class CalculadoraSplitPagamento {

    public ResultadoSplit processarSplit(
            BigDecimal valorTotal,
            BigDecimal taxaClinicaInfraestrutura,
            BigDecimal repasseProfissional
    ) {
        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor_total do pagamento deve ser maior que zero.");
        }

        BigDecimal totalNormalizado = valorTotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxaNormalizada;
        BigDecimal repasseNormalizado;

        if (taxaClinicaInfraestrutura != null && repasseProfissional == null) {
            taxaNormalizada = taxaClinicaInfraestrutura.setScale(2, RoundingMode.HALF_UP);
            repasseNormalizado = totalNormalizado.subtract(taxaNormalizada);
        } else if (taxaClinicaInfraestrutura == null && repasseProfissional != null) {
            repasseNormalizado = repasseProfissional.setScale(2, RoundingMode.HALF_UP);
            taxaNormalizada = totalNormalizado.subtract(repasseNormalizado);
        } else if (taxaClinicaInfraestrutura != null) {
            taxaNormalizada = taxaClinicaInfraestrutura.setScale(2, RoundingMode.HALF_UP);
            repasseNormalizado = repasseProfissional.setScale(2, RoundingMode.HALF_UP);
        } else {
            throw new RegraNegocioException(
                    "Informe a taxa_clinica_infraestrutura e/ou o repasse_profissional para realizar o split."
            );
        }

        if (taxaNormalizada.compareTo(BigDecimal.ZERO) < 0 || repasseNormalizado.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Os valores de split não podem ser negativos.");
        }

        BigDecimal somaSplit = taxaNormalizada.add(repasseNormalizado);
        if (somaSplit.compareTo(totalNormalizado) != 0) {
            throw new RegraNegocioException(String.format(
                    "Inconsistência contábil no split: taxa_clinica_infraestrutura (%s) + repasse_profissional (%s) difere do valor_total (%s).",
                    taxaNormalizada, repasseNormalizado, totalNormalizado
            ));
        }

        return new ResultadoSplit(totalNormalizado, taxaNormalizada, repasseNormalizado);
    }

    public record ResultadoSplit(
            BigDecimal valorTotal,
            BigDecimal taxaClinicaInfraestrutura,
            BigDecimal repasseProfissional
    ) {
    }
}
