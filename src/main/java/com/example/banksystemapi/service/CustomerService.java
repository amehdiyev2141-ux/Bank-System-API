package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.CustomerRequest;
import com.example.banksystemapi.dto.CustomerResponse;
import com.example.banksystemapi.entity.Customer;
import com.example.banksystemapi.enums.CustomerStatus;
import com.example.banksystemapi.exception.CustomerNotFoundException;
import com.example.banksystemapi.exception.EmailAlreadyExistsException;
import com.example.banksystemapi.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        String email = request.getEmail().trim();
        if (customerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        Customer customer = new Customer();

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(email);
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponse(savedCustomer);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();

        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setCreatedAt(customer.getCreatedAt());
        response.setStatus(customer.getStatus());

        return response;
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + id
                ));

        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + id
                ));

        String email = request.getEmail().trim();
        if (customerRepository.existsByEmailAndIdNot(email, id)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(email);
        customer.setPhoneNumber(request.getPhoneNumber());

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponse(updatedCustomer);
    }

    @Transactional
    public CustomerResponse updateCustomerStatus(Long id, CustomerStatus status) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + id
                ));

        customer.setStatus(status);
        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponse(updatedCustomer);
    }
}


