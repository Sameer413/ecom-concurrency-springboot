package org.example.flashsale.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddressRequest {
    @NotNull(message = "Address ID is required")
    private UUID addressId;
    @NotBlank(message = "Recipient full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Pattern(regexp = "^[+0-9\\-\\s()]*$", message = "Invalid phone number format")
    private String phone;
}
