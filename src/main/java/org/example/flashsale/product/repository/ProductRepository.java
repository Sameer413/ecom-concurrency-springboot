package org.example.flashsale.product.repository;

import org.example.flashsale.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.stereotype.Repository;

import java.util.UUID;

//@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"variants"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
}
