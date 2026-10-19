package org.example.flashsale.product.service;

import org.apache.coyote.BadRequestException;
import org.example.flashsale.common.exception.ConflictException;
import org.example.flashsale.common.exception.DuplicateResourceException;
import org.example.flashsale.common.exception.ResourceNotFoundException;
import org.example.flashsale.common.exception.UnauthorizedException;
import org.example.flashsale.common.infrastructure.redis.RedisService;
import org.example.flashsale.common.persistence.BaseEntity;
import org.example.flashsale.inventory.dto.InventoryResponse;
import org.example.flashsale.inventory.service.InventoryService;
import org.example.flashsale.product.dto.*;
import org.example.flashsale.product.entity.Product;
import org.example.flashsale.product.entity.ProductVariant;
import org.example.flashsale.product.repository.ProductRepository;
import org.example.flashsale.product.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final RedisService redisService;
    private final InventoryService inventoryService;

    public ProductService(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            RedisService redisService,
            InventoryService inventoryService
    ) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.redisService = redisService;
        this.inventoryService = inventoryService;
    }


//    Detail view - attach inventory with 1 bulk DB call
    @Transactional(readOnly = true)
    public ProductResponse findProductById(UUID uuid) {
        Product product = productRepository.findById(uuid).orElseThrow(
                () -> new ResourceNotFoundException("product with id: " + uuid)
        );

        // 1. Collect all variant Ids
        List<UUID> variantIds = product.getVariants()
                                    .stream()
                                    .map(ProductVariant::getId)
                                    .toList();

        // 2. One DB call for all inventories
        Map<UUID, InventoryResponse> inventoryMap = inventoryService.getInventory(variantIds);

        // 3. Map inventory with their variant
        List<ProductVariantResponse> variants = product.getVariants()
                .stream()
                .map(v -> ProductVariantResponse.from(v, inventoryMap.get(v.getId())))
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlug(),
                product.getBrand(),
                product.getCategory(),
                product.getStatus(),
                product.getBasePrice(),
                variants
        );
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest dto) {
        // 1. Validate Slug
        if(productRepository.existsBySlug(dto.getSlug())) {
            throw new DuplicateResourceException("Product slug already exists");
        }

        // 2. Validate variant SKUs
        validateVariantSkus(dto.getVariants());

        // 3. Create Product
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .slug(dto.getSlug())
                .brand(dto.getBrand())
                .category(dto.getCategory())
                .status(dto.getStatus())
                .basePrice(dto.getBasePrice())
                .build();

        product = productRepository.save(product);

        // 4. Create Variants
        Product finalProduct = product;
        List<ProductVariant> productVariants =
                dto.getVariants()
                        .stream()
                        .map(variant ->
                                createVariant(finalProduct, variant))
                        .toList();

        productVariantRepository.saveAll(productVariants);

        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse deleteProductById(UUID productId, UUID userId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("product with id: " + productId)
        );

        if(!product.getCreatedBy().getId().equals(userId)) {
            throw new UnauthorizedException("Operation not permitted");
        }

        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

        List<UUID> variantIds = variants.stream().map(BaseEntity::getId).toList();

        Map<UUID, InventoryResponse> inventoryMap = inventoryService.getInventory(variantIds);


        int variantDeleteCount = productVariantRepository.deleteAllByIds(variantIds);
        int productDeleteCount = inventoryService.deleteInventories(inventoryMap.keySet());

        if(variantDeleteCount != variants.size() && productDeleteCount != inventoryMap.size()) {
            throw new ConflictException("Internal Server Error");
        }

        productRepository.deleteById(productId);

        return ProductResponse.from(product);
    }

    private ProductVariant createVariant(
            Product product,
            CreateProductVariantRequest request) {

        return ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .attributes(request.getAttributes())
                .price(request.getPrice())
                .weightGrams(request.getWeightGrams())
                .isActive(request.getIsActive())
                .build();
    }

    private void validateVariantSkus(
            List<CreateProductVariantRequest> variants) {

        Set<String> skus = new HashSet<>();

        for (CreateProductVariantRequest variant : variants) {

            if (!skus.add(variant.getSku())) {
                throw new DuplicateResourceException(
                        "Duplicate SKU: " + variant.getSku()
                );
            }
        }
    }

}
