package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.CustomerRequest;
import com.example.banksystemapi.dto.CustomerResponse;
import com.example.banksystemapi.dto.CustomerStatusRequest;
import com.example.banksystemapi.service.CustomerService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer registration, lookup, updates, and status management")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a customer")
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping
    @Operation(summary = "List customers")
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by ID")
    public CustomerResponse getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details")
    public CustomerResponse updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update customer status")
    public CustomerResponse updateCustomerStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusRequest request) {
        return customerService.updateCustomerStatus(id, request.getStatus());
    }

}
