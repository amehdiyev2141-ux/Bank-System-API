package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.CustomerRequest;
import com.example.banksystemapi.dto.CustomerResponse;
import com.example.banksystemapi.enums.CustomerStatus;
import com.example.banksystemapi.exception.CustomerNotFoundException;
import com.example.banksystemapi.exception.EmailAlreadyExistsException;
import com.example.banksystemapi.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void createCustomerReturnsCreatedCustomer() throws Exception {
        CustomerResponse response = new CustomerResponse();
        response.setId(1L);
        response.setFirstName("Aylin");
        response.setLastName("Aliyeva");
        response.setEmail("aylin@example.com");
        response.setCreatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        response.setStatus(CustomerStatus.ACTIVE);
        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Aylin","lastName":"Aliyeva","email":"aylin@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("aylin@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createCustomerReportsInvalidFields() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":" ","lastName":"Aliyeva","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void createCustomerReportsDuplicateEmail() throws Exception {
        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Aylin","lastName":"Aliyeva","email":"aylin@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void createCustomerReportsDatabaseConstraintConflict() throws Exception {
        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Aylin","lastName":"Aliyeva","email":"aylin@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Data conflicts with an existing record"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void getAllCustomersReturnsList() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCustomerByIdReturnsCustomer() throws Exception {
        CustomerResponse response = new CustomerResponse();
        response.setId(7L);
        response.setFirstName("Aylin");
        response.setLastName("Aliyeva");
        response.setEmail("aylin@example.com");
        response.setStatus(CustomerStatus.ACTIVE);
        when(customerService.getCustomerById(7L)).thenReturn(response);

        mockMvc.perform(get("/api/customers/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("aylin@example.com"));
    }

    @Test
    void getCustomerByIdReturnsNotFound() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 99"));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 99"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void updateCustomerReturnsUpdatedCustomer() throws Exception {
        CustomerResponse response = new CustomerResponse();
        response.setId(7L);
        response.setFirstName("Aylin Updated");
        response.setLastName("Aliyeva");
        response.setEmail("aylin@example.com");
        response.setStatus(CustomerStatus.ACTIVE);
        when(customerService.updateCustomer(eq(7L), any(CustomerRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/customers/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Aylin Updated","lastName":"Aliyeva","email":"aylin@example.com","phoneNumber":"+994501234567"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.firstName").value("Aylin Updated"));
    }

    @Test
    void updateCustomerReturnsNotFound() throws Exception {
        when(customerService.updateCustomer(eq(99L), any(CustomerRequest.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 99"));

        mockMvc.perform(put("/api/customers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Aylin","lastName":"Aliyeva","email":"aylin@example.com"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 99"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void updateCustomerReportsDuplicateEmail() throws Exception {
        when(customerService.updateCustomer(eq(7L), any(CustomerRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(put("/api/customers/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Aylin","lastName":"Aliyeva","email":"other@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void updateCustomerReportsInvalidRequest() throws Exception {
        mockMvc.perform(put("/api/customers/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":" ","lastName":"Aliyeva","email":"invalid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void updateCustomerStatusReturnsUpdatedCustomer() throws Exception {
        CustomerResponse response = new CustomerResponse();
        response.setId(7L);
        response.setFirstName("Aylin");
        response.setStatus(CustomerStatus.BLOCKED);
        when(customerService.updateCustomerStatus(7L, CustomerStatus.BLOCKED))
                .thenReturn(response);

        mockMvc.perform(patch("/api/customers/7/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"BLOCKED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void updateCustomerStatusReturnsNotFound() throws Exception {
        when(customerService.updateCustomerStatus(99L, CustomerStatus.INACTIVE))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 99"));

        mockMvc.perform(patch("/api/customers/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"INACTIVE"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 99"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void updateCustomerStatusRejectsNullStatus() throws Exception {
        mockMvc.perform(patch("/api/customers/7/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.status").exists());
    }
}
