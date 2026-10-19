package org.example.flashsale.order.repository;

import org.example.flashsale.order.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, UUID> {
    Optional<ShippingAddress> findByOrderId(UUID orderId);
}
