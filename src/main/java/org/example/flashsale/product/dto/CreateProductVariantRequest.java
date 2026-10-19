package org.example.flashsale.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductVariantRequest {

    @NotBlank
    @Size(max = 100)
    private String sku;

    private Map<String, String> attributes;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @Min(0)
    private Integer weightGrams;

    @NotNull
    @Builder.Default
    private Boolean isActive = true;
}
