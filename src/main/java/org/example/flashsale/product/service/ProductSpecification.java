package org.example.flashsale.product.service;

import jakarta.persistence.criteria.Predicate;
import org.example.flashsale.product.dto.ProductFilterRequest;
import org.example.flashsale.product.entity.Product;
import org.example.flashsale.product.entity.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filter(
            ProductFilterRequest request
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always show ACTIVE products
            predicates.add(
                    cb.equal(
                            root.get("status"),
                            ProductStatus.ACTIVE
                    )
            );

            if(request.getSearch() != null && !request.getSearch().isBlank()) {
                String search = "%" + request.getSearch().toLowerCase() + "%";

                Predicate name = cb.like(
                        cb.lower(root.get("name")),
                        search
                );

                Predicate description = cb.like(
                        cb.lower(root.get("description")), search
                );

                Predicate brand = cb.like(
                        cb.lower(root.get("brand")), search
                );

                predicates.add(
                        cb.or(name, description, brand)
                );
            }

            if(request.getCategory() != null && !request.getCategory().isBlank()) {
//                String category = "%" + request.getCategory() + "%";

                predicates.add(
                        cb.equal(
                                cb.lower(root.get("category")), request.getCategory().toLowerCase()
                        )
                );
            }

            if (request.getBrand() != null &&
                    !request.getBrand().isBlank()) {

                predicates.add(
                        cb.equal(
                                root.get("brand"),
                                request.getBrand()
                        )
                );
            }

            if (request.getMinPrice() != null) {

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("basePrice"),
                                request.getMinPrice()
                        )
                );
            }

            if (request.getMaxPrice() != null) {

                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("basePrice"),
                                request.getMaxPrice()
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
