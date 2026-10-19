package org.example.flashsale.address.service;

import org.example.flashsale.address.dto.AddressResponse;
import org.example.flashsale.address.dto.CreateAddressRequest;
import org.example.flashsale.address.entity.Address;
import org.example.flashsale.address.repository.AddressRepository;
import org.example.flashsale.common.exception.ResourceNotFoundException;
import org.example.flashsale.user.entity.User;
import org.example.flashsale.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }
    public AddressResponse createAddress(CreateAddressRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(true)
                .build();

        Address saved = addressRepository.save(address);

        return AddressResponse.mapTo(saved);
    }

    public Address findById(UUID id) {
        return addressRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Address not found with id: " + id)
        );
    }

    public List<AddressResponse> findAllByUserId(UUID userId) {
        List<Address> addresses = addressRepository.findAllByUser_Id(userId);

        if (addresses.isEmpty()) {
            return List.of();
        }

        return addresses.stream()
                .map(AddressResponse::mapTo)
                .toList();
    }
}
