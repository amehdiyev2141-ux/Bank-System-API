package com.example.banksystemapi.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerTests {

    @Test
    void createdAtIsSetBeforePersistenceWhenMissing() {
        Customer customer = new Customer();

        customer.onCreate();

        assertNotNull(customer.getCreatedAt());
    }

    @Test
    void createdAtIsPreservedWhenAlreadySet() {
        LocalDateTime createdAt = LocalDateTime.parse("2026-01-01T10:00:00");
        Customer customer = new Customer();
        customer.setCreatedAt(createdAt);

        customer.onCreate();

        assertEquals(createdAt, customer.getCreatedAt());
    }
}
