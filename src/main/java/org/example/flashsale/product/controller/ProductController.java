package org.example.flashsale.product.controller;

import org.example.flashsale.auth.entity.UserPrincipal;
import org.example.flashsale.common.persistence.ApiResponse;
import org.example.flashsale.product.dto.CreateProductRequest;
import org.example.flashsale.product.dto.ProductFilterRequest;
import org.example.flashsale.product.dto.ProductResponse;
import org.example.flashsale.product.entity.ProductStatus;
import org.example.flashsale.product.service.ProductSearchService;
import org.example.flashsale.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    public ProductController(ProductService productService, ProductSearchService productSearchService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String category,

            @RequestParam(required = false)
            String brand,

            @RequestParam(required = false)
            ProductStatus status,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(defaultValue = "newest")
            String sort
    ) {
        ProductFilterRequest request =
                new ProductFilterRequest();

        request.setSearch(search);
        request.setCategory(category);
        request.setBrand(brand);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setPage(page);
        request.setSize(size);
        request.setSort(sort);

        Page<ProductResponse> result =
                productSearchService.findAllProducts(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Products fetched successfully",
                        result
                )
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable(name = "productId") UUID productId) {
        ProductResponse response = productService.findProductById(productId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Product fetched successfully", response)
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductResponse>> createProductHandler(@RequestBody CreateProductRequest dto) {
        ProductResponse response = productService.createProduct(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(true,"Product created successfully", response)
        );
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteProductHandler(
            @PathVariable(name = "productId") UUID productId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        ProductResponse response = productService.deleteProductById(productId, user.getId());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Product deleted successfully", response)
        );
    }
}
