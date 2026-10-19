package org.example.flashsale.product.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record ProductPageCache(
        List<ProductResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ProductPageCache from(
            Page<ProductResponse> page
    ) {
        return new ProductPageCache(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public Page<ProductResponse> toPage() {

        Pageable pageable =
                PageRequest.of(page, size);

        return new PageImpl<>(
                content,
                pageable,
                totalElements
        );
    }
}
