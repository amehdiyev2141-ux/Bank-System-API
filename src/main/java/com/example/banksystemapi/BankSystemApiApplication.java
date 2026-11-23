package com.example.banksystemapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Banking System API",
        version = "v1",
        description = "REST API for customers, bank accounts, transactions, transfers, and cards"))
public class BankSystemApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankSystemApiApplication.class, args);
    }

}
