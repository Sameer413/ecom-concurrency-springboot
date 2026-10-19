package org.example.flashsale.address.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.flashsale.address.entity.Address;

import java.time.Instant;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private UUID id;
    private UUID userId;
    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;

    public static AddressResponse mapTo(Address saved) {
        return AddressResponse.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())
                .label(saved.getLabel())
                .line1(saved.getLine1())
                .line2(saved.getLine2())
                .city(saved.getCity())
                .state(saved.getState())
                .postalCode(saved.getPostalCode())
                .country(saved.getCountry())
                .isDefault(saved.getIsDefault())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
