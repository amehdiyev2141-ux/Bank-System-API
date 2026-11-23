package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.CustomerRequest;
import com.example.banksystemapi.dto.CustomerResponse;
import com.example.banksystemapi.entity.Customer;
import com.example.banksystemapi.enums.CustomerStatus;
import com.example.banksystemapi.exception.CustomerNotFoundException;
import com.example.banksystemapi.exception.EmailAlreadyExistsException;
import com.example.banksystemapi.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerServiceTests {

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final CustomerService customerService = new CustomerService(customerRepository);

    @Test
    void createCustomerSetsBackendManagedValuesAndMapsSavedCustomer() {
        LocalDateTime createdAt = LocalDateTime.parse("2026-01-01T10:00:00");
        when(customerRepository.existsByEmail("aylin@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(42L);
            customer.setCreatedAt(createdAt);
            return customer;
        });

        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Aylin");
        request.setLastName("Aliyeva");
        request.setEmail(" aylin@example.com ");
        request.setPhoneNumber("+994501234567");

        CustomerResponse response = customerService.createCustomer(request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertEquals(CustomerStatus.ACTIVE, savedCustomer.getStatus());
        assertEquals("aylin@example.com", savedCustomer.getEmail());
        assertEquals(42L, response.getId());
        assertEquals(CustomerStatus.ACTIVE, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals("Aylin", response.getFirstName());
    }

    @Test
    void updateCustomerAllowsItsExistingEmailAndPreservesBackendFields() {
        Long id = 7L;
        LocalDateTime createdAt = LocalDateTime.parse("2026-01-01T10:00:00");
        Customer existingCustomer = customer(id, createdAt, CustomerStatus.BLOCKED);
        when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmailAndIdNot("aylin@example.com", id)).thenReturn(false);
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        CustomerRequest request = updateRequest("aylin@example.com");
        CustomerResponse response = customerService.updateCustomer(id, request);

        verify(customerRepository).existsByEmailAndIdNot("aylin@example.com", id);
        assertEquals(id, response.getId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(CustomerStatus.BLOCKED, response.getStatus());
        assertEquals("Aylin Updated", response.getFirstName());
    }

    @Test
    void updateCustomerRejectsEmailOwnedByAnotherCustomer() {
        Long id = 7L;
        Customer existingCustomer = customer(id,
                LocalDateTime.parse("2026-01-01T10:00:00"), CustomerStatus.ACTIVE);
        when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmailAndIdNot("other@example.com", id)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> customerService.updateCustomer(id, updateRequest(" other@example.com ")));
    }

    @Test
    void updateCustomerRejectsMissingCustomer() {
        Long id = 99L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(id, updateRequest("aylin@example.com")));
    }

    @Test
    void updateCustomerStatusChangesStatusAndReturnsResponse() {
        Long id = 7L;
        Customer existingCustomer = customer(id,
                LocalDateTime.parse("2026-01-01T10:00:00"), CustomerStatus.ACTIVE);
        when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        CustomerResponse response = customerService.updateCustomerStatus(id, CustomerStatus.BLOCKED);

        assertEquals(CustomerStatus.BLOCKED, existingCustomer.getStatus());
        assertEquals(CustomerStatus.BLOCKED, response.getStatus());
        assertEquals(id, response.getId());
    }

    private Customer customer(Long id, LocalDateTime createdAt, CustomerStatus status) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("Aylin");
        customer.setLastName("Aliyeva");
        customer.setEmail("aylin@example.com");
        customer.setCreatedAt(createdAt);
        customer.setStatus(status);
        return customer;
    }

    private CustomerRequest updateRequest(String email) {
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Aylin Updated");
        request.setLastName("Aliyeva");
        request.setEmail(email);
        request.setPhoneNumber("+994501234567");
        return request;
    }
}
