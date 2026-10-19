package org.example.flashsale.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressRequest {
    @Size(max = 50)
    private String label;               // e.g. "Home", "Office" — optional
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255)
    private String line1;
    @Size(max = 255)
    private String line2;               // optional
    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;
    @Size(max = 100)
    private String state;               // optional
    @Size(max = 20)
    private String postalCode;          // optional
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be a 2-letter ISO code")
    private String country;
    private Boolean isDefault;          // optional, defaults to false in entity
}
