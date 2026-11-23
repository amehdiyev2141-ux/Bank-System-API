package com.example.banksystemapi;

import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.entity.BankCard;
import com.example.banksystemapi.entity.Customer;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.Currency;
import com.example.banksystemapi.enums.CustomerStatus;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.BankCardRepository;
import com.example.banksystemapi.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BankingModulesIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Test
    void transferEndpointUpdatesBothPersistedAccountBalances() throws Exception {
        Customer customer = createCustomer();
        BankAccount source = createAccount(customer, "100.00");
        BankAccount destination = createAccount(customer, "20.00");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceAccountId":%d,"destinationAccountId":%d,"amount":35.00}
                                """.formatted(source.getId(), destination.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.sourceAccountId").value(source.getId()))
                .andExpect(jsonPath("$.destinationAccountId").value(destination.getId()));

        assertEquals(new BigDecimal("65.00"),
                bankAccountRepository.findById(source.getId()).orElseThrow().getBalance());
        assertEquals(new BigDecimal("55.00"),
                bankAccountRepository.findById(destination.getId()).orElseThrow().getBalance());
    }

    @Test
    void cardEndpointPersistsPanAndReturnsOnlyMaskedValue() throws Exception {
        BankAccount account = createAccount(createCustomer(), "0.00");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bankAccountId":%d}
                                """.formatted(account.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maskedCardNumber").exists())
                .andExpect(jsonPath("$.cardNumber").doesNotExist())
                .andExpect(jsonPath("$.cvv").doesNotExist());

        BankCard savedCard = bankCardRepository.findAllByBankAccount_Id(account.getId())
                .getFirst();
        assertEquals(16, savedCard.getCardNumber().length());
    }

    private Customer createCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setEmail(UUID.randomUUID() + "@example.com");
        customer.setStatus(CustomerStatus.ACTIVE);
        return customerRepository.save(customer);
    }

    private BankAccount createAccount(Customer customer, String balance) {
        BankAccount account = new BankAccount();
        account.setAccountNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        account.setBalance(new BigDecimal(balance));
        account.setCurrency(Currency.AZN);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);
        return bankAccountRepository.save(account);
    }
}
