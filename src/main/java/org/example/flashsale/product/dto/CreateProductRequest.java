package org.example.flashsale.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.flashsale.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank
    @Size(max = 160)
    private String name;

    private String description;

    @NotBlank
    @Size(max = 160)
    private String slug;

    @Size(max = 100)
    private String brand;

    @Size(max = 100)
    private String category;

    @NotNull
    private ProductStatus status;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal basePrice;

    @Valid
    @NotEmpty
    private List<CreateProductVariantRequest> variants;
}
