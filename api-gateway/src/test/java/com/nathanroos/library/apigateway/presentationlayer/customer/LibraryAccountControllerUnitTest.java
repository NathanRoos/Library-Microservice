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
class LibraryAccountControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAccountById_Success() throws Exception {
        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder()
                .accountId("account-1")
                .firstname("John")
                .lastname("Doe")
                .build();

        when(customerService.getAccountByAccountId("account-1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/account-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value("account-1"));
    }

    @Test
    void testGetAccountById_NotFound() throws Exception {
        when(customerService.getAccountByAccountId(anyString()))
                .thenThrow(new NotFoundException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/notfound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllAccounts() throws Exception {
        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder()
                .accountId("account-1")
                .firstname("John")
                .lastname("Doe")
                .build();

        when(customerService.getAllAccounts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value("account-1"));
    }

    @Test
    void testAddAccount_Success() throws Exception {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .phoneNumber("123-456-7890")
                .email("jane.smith@example.com")
                .build();

        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder()
                .accountId("account-2")
                .firstname("Jane")
                .lastname("Smith")
                .build();

        when(customerService.addAccount(any(LibraryAccountRequestModel.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("account-2"));
    }

    @Test
    void testAddAccount_InvalidInput() throws Exception {
        when(customerService.addAccount(any(LibraryAccountRequestModel.class)))
                .thenThrow(new InvalidInputException("Invalid input"));

        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .build(); // Missing phone and email

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUpdateAccount_Success() throws Exception {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("UpdatedFirst")
                .lastname("UpdatedLast")
                .phoneNumber("987-654-3210")
                .email("updated@example.com")
                .build();

        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder()
                .accountId("account-1")
                .firstname("UpdatedFirst")
                .lastname("UpdatedLast")
                .build();

        when(customerService.updateAccount(any(LibraryAccountRequestModel.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/accounts/account-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("account-1"));
    }

    @Test
    void testDeleteAccount() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/account-1"))
                .andExpect(status().isNoContent());
    }
}
