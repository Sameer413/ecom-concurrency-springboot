package org.example.flashsale.address.repository;


import org.example.flashsale.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

//@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findAllByUser_Id(UUID userId);
}
