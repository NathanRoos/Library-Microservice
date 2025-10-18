package com.nathanroos.library.apigateway.presentationlayer.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.businesslayer.customer.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibraryAccountController.class)
class LibraryAccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAccountById_Success() throws Exception {
        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder()
                .accountId("account-123")
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("123-456-7890")
                .build();

        when(customerService.getAccountByAccountId("account-123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/account-123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value("account-123"));
    }

    @Test
    void testGetAccountById_NotFound() throws Exception {
        when(customerService.getAccountByAccountId("account-999"))
                .thenThrow(new NotFoundException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/account-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllAccounts() throws Exception {
        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder()
                .accountId("account-123")
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("123-456-7890")
                .build();

        when(customerService.getAllAccounts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value("account-123"));
    }

    @Test
    void testAddAccount() throws Exception {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("987-654-3210")
                .build();

        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder()
                .accountId("account-456")
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("987-654-3210")
                .build();

        when(customerService.addAccount(any(LibraryAccountRequestModel.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("account-456"));
    }

    @Test
    void testAddAccount_InvalidInput() throws Exception {
        when(customerService.addAccount(any(LibraryAccountRequestModel.class)))
                .thenThrow(new InvalidInputException("Invalid input"));

        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("Jane")
                .build(); // Missing required fields

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUpdateAccount() throws Exception {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("UpdatedFirst")
                .lastname("UpdatedLast")
                .email("updated.email@example.com")
                .phoneNumber("555-555-5555")
                .build();

        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder()
                .accountId("account-123")
                .firstname("UpdatedFirst")
                .lastname("UpdatedLast")
                .email("updated.email@example.com")
                .phoneNumber("555-555-5555")
                .build();

        when(customerService.updateAccount(any(LibraryAccountRequestModel.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/accounts/account-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("account-123"));
    }

    @Test
    void testDeleteAccount() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/account-123"))
                .andExpect(status().isNoContent());
    }
}
