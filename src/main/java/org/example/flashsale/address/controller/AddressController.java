package org.example.flashsale.address.controller;

import jakarta.validation.Valid;
import org.example.flashsale.address.dto.AddressResponse;
import org.example.flashsale.address.dto.CreateAddressRequest;
import org.example.flashsale.address.service.AddressService;
import org.example.flashsale.auth.entity.UserPrincipal;
import org.example.flashsale.common.persistence.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

    private final AddressService addressService;
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddressHandler(
            @Valid @RequestBody CreateAddressRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {
        AddressResponse address = addressService.createAddress(request, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(true,"Address created successfully", address)
        );
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<AddressResponse>>> findAllAddressHandler(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        List<AddressResponse> response = addressService.findAllByUserId(userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Address fetched successfully", response)
        );
    }
}
