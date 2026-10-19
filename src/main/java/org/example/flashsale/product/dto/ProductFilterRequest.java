package org.example.flashsale.product.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.flashsale.product.entity.ProductStatus;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductFilterRequest {

    private String search;

    private String category;

    private String brand;

//    private ProductStatus status;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer page = 0;

    private Integer size = 20;

    private String sort = "createdAt";

    private String direction = "desc";
}