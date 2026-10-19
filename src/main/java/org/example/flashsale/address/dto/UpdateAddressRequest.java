package org.example.flashsale.address.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressRequest {
    // All fields optional on update — only non-null fields will be applied
    @Size(max = 50)
    private String label;
    @Size(max = 255)
    private String line1;
    @Size(max = 255)
    private String line2;
    @Size(max = 100)
    private String city;
    @Size(max = 100)
    private String state;
    @Size(max = 20)
    private String postalCode;
    @Size(min = 2, max = 2, message = "Country must be a 2-letter ISO code")
    private String country;
    private Boolean isDefault;
}