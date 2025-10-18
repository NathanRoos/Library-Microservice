package com.nathanroos.library.apigateway.businesslayer.customer;

import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.domainclientlayer.customer.CustomerServiceClient;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CustomerServiceUnitTest {

    @Mock
    private CustomerServiceClient customerServiceClient;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private LibraryAccountRequestModel requestModel;
    private LibraryAccountResponseModel responseModel;

    @BeforeEach
    void setUp() {
        String accountId = UUID.randomUUID().toString();

        requestModel = LibraryAccountRequestModel.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("123-456-7890")
                .build();

        responseModel = LibraryAccountResponseModel.builder()
                .accountId(accountId)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("123-456-7890")
                .build();
    }

    @Test
    void whenGetAccountById_thenReturnAccount() {
        when(customerServiceClient.getAccountByAccountId(responseModel.getAccountId()))
                .thenReturn(responseModel);

        var result = customerService.getAccountByAccountId(responseModel.getAccountId());

        assertNotNull(result);
        assertEquals(responseModel.getAccountId(), result.getAccountId());
        assertEquals(responseModel.getFirstname(), result.getFirstname());
        assertEquals(responseModel.getLastname(), result.getLastname());
    }

    @Test
    void whenGetAllAccounts_thenReturnList() {
        when(customerServiceClient.getAccounts()).thenReturn(List.of(responseModel));

        var result = customerService.getAllAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseModel.getAccountId(), result.get(0).getAccountId());
    }

    @Test
    void whenGetAllAccountsIsEmpty_thenReturnEmptyList() {
        when(customerServiceClient.getAccounts()).thenReturn(Collections.emptyList());

        var result = customerService.getAllAccounts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenAddAccount_thenReturnCreatedAccount() {
        when(customerServiceClient.addAccount(requestModel)).thenReturn(responseModel);

        var result = customerService.addAccount(requestModel);

        assertNotNull(result);
        assertEquals(responseModel.getAccountId(), result.getAccountId());
    }

    @Test
    void whenUpdateAccount_thenReturnUpdatedAccount() {
        when(customerServiceClient.updateAccount(requestModel, responseModel.getAccountId()))
                .thenReturn(responseModel);

        var result = customerService.updateAccount(requestModel, responseModel.getAccountId());

        assertNotNull(result);
        assertEquals(responseModel.getAccountId(), result.getAccountId());
    }

    @Test
    void whenDeleteAccount_thenVerifyClientCall() {
        doNothing().when(customerServiceClient).removeAccount(responseModel.getAccountId());

        customerService.removeAccount(responseModel.getAccountId());

        verify(customerServiceClient, times(1)).removeAccount(responseModel.getAccountId());
    }

    @Test
    void whenGetAccountByIdNotFound_thenThrowNotFoundException() {
        when(customerServiceClient.getAccountByAccountId(anyString())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            customerService.getAccountByAccountId("nonexistent-id");
        });
    }

    @Test
    void whenCustomerServiceClientThrowsException_thenPropagate() {
        when(customerServiceClient.getAccountByAccountId(anyString()))
                .thenThrow(new RuntimeException("Service failure"));

        assertThrows(RuntimeException.class, () -> {
            customerService.getAccountByAccountId("some-id");
        });
    }
}
