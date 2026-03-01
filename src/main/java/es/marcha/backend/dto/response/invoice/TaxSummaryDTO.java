package es.marcha.backend.dto.response.invoice;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TaxSummaryDTO {

    private BigDecimal percent;
    private BigDecimal base;
    private BigDecimal amount;
}
