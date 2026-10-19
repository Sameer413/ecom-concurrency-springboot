package org.example.flashsale.product.service;

import org.example.flashsale.common.infrastructure.redis.RedisService;
import org.example.flashsale.product.dto.ProductFilterRequest;
import org.example.flashsale.product.dto.ProductPageCache;
import org.example.flashsale.product.dto.ProductResponse;
import org.example.flashsale.product.entity.Product;
import org.example.flashsale.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductSearchService {
    private final ProductRepository productRepository;
    private final RedisService redisService;

    public ProductSearchService(
            ProductRepository productRepository,
            RedisService redisService
    ) {
        this.productRepository = productRepository;
        this.redisService = redisService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAllProducts(ProductFilterRequest dto) {

        String cacheKey = generateRedisKey(dto);

        ProductPageCache cached = redisService.cacheAside(
                cacheKey,
                ProductPageCache.class,
                () -> fetchProductsFromDatabase(dto),
                300
        );

        return cached.toPage();
    }

    private String generateRedisKey(ProductFilterRequest dto) {

        String sortCode = switch (dto.getSort()) {
            case "price" -> "p";
            case "newest" -> "n";
            case "oldest" -> "o";
            case "name" -> "m";
            default -> "d";
        };

        String directionCode;

        if ("price".equals(dto.getSort())) {
            directionCode =
                    "asc".equalsIgnoreCase(dto.getDirection())
                            ? "a"
                            : "d";
        } else if ("oldest".equals(dto.getSort())) {
            directionCode = "a";
        } else {
            directionCode = "d";
        }

        return "p:"
                + dto.getPage()
                + ":"
                + dto.getSize()
                + ":"
                + sortCode
                + directionCode
                + ":"
                + dto.getCategory();
    }

    private ProductPageCache fetchProductsFromDatabase(ProductFilterRequest dto) {
        Sort sort = getSort(
                dto.getSort(),
                dto.getDirection()
        );

        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), sort);

        Specification<Product> specification = ProductSpecification.filter(dto);

        Page<Product> products = productRepository.findAll(specification, pageable);

        Page<ProductResponse> response = products.map(this::toResponse);

        return ProductPageCache.from(response);
    }

    private Sort getSort(
            String sort,
            String direction
    ) {
        String field = switch (sort) {

            case "price" -> "basePrice";

            case "newest", "oldest" -> "createdAt";

            case "name" -> "name";

            default -> "createdAt";
        };

        Sort.Direction sortDirection;

        if("price".equals(sort)){
            sortDirection =
                    "asc".equalsIgnoreCase(direction)
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;
        } else if("oldest".equals(sort)){
            sortDirection = Sort.Direction.ASC;
        } else {
            sortDirection = Sort.Direction.DESC;
        }

        return Sort.by(sortDirection, field);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.from(product);
    }
}
