package org.example.flashsale.inventory.repository;

import jakarta.persistence.LockModeType;
import org.example.flashsale.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByProductVariantId(UUID productVariantId);
    List<Inventory> findByProductVariantIdIn(Collection<UUID> variantIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i
        FROM Inventory i
        WHERE i.productVariant.id = :variantId
    """)
    Optional<Inventory> findByProductVariantIdForUpdate(@Param("variantId") UUID productVariantId);

    @Modifying
    @Query("DELETE FROM Inventory inv WHERE inv.productVariant.id IN :variantIds")
    int deleteAllByIds(@Param("variantIds") Collection<UUID> variantIds);
}
