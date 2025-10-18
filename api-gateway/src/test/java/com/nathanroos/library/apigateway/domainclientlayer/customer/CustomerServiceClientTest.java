package com.nathanroos.library.apigateway.domainclientlayer.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.HttpErrorInfo;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceClientTest {

    @InjectMocks
    private CustomerServiceClient customerServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private String host = "localhost";
    private String port = "8080";

    @BeforeEach
    void setup() {
        customerServiceClient = new CustomerServiceClient(restTemplate, objectMapper, host, port);
    }

    @Test
    void testGetAccountById_Success() {
        String accountId = "acc123";
        LibraryAccountResponseModel mockAccount = LibraryAccountResponseModel.builder()
                .accountId(accountId)
                .firstname("John")
                .lastname("Doe")
                .build();
        when(restTemplate.getForObject(anyString(), eq(LibraryAccountResponseModel.class))).thenReturn(mockAccount);

        LibraryAccountResponseModel result = customerServiceClient.getAccountByAccountId(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals("John", result.getFirstname());
    }

    @Test
    void testGetAllAccounts_Success() {
        List<LibraryAccountResponseModel> accounts = List.of(LibraryAccountResponseModel.builder().accountId("acc123").build());
        ResponseEntity<List<LibraryAccountResponseModel>> response = new ResponseEntity<>(accounts, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryAccountResponseModel> result = customerServiceClient.getAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testAddAccount_Success() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder().firstname("John").lastname("Doe").build();
        LibraryAccountResponseModel response = LibraryAccountResponseModel.builder().accountId("acc123").build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryAccountResponseModel.class))).thenReturn(response);

        LibraryAccountResponseModel result = customerServiceClient.addAccount(request);

        assertNotNull(result);
    }

    @Test
    void testUpdateAccount_Success() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder().firstname("John").lastname("Doe").build();
        LibraryAccountResponseModel updated = LibraryAccountResponseModel.builder().accountId("acc123").firstname("John").build();
        String accountId = "acc123";

        when(restTemplate.getForObject(anyString(), eq(LibraryAccountResponseModel.class))).thenReturn(updated);

        LibraryAccountResponseModel result = customerServiceClient.updateAccount(request, accountId);

        assertNotNull(result);
        assertEquals("John", result.getFirstname());
    }

    @Test
    void testDeleteAccount_Success() {
        doNothing().when(restTemplate).delete(anyString());

        assertDoesNotThrow(() -> customerServiceClient.removeAccount("acc123"));
    }

    @Test
    void testGetAccountById_NotFound() throws Exception {
        String accountId = "acc123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryAccountResponseModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/accounts/" + accountId));

        assertThrows(NotFoundException.class, () -> customerServiceClient.getAccountByAccountId(accountId));
    }

    @Test
    void testAddAccount_InvalidInput() throws Exception {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder().firstname("John").lastname("Doe").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryAccountResponseModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/accounts"));

        assertThrows(InvalidInputException.class, () -> customerServiceClient.addAccount(request));
    }

    @Test
    void testDeleteAccount_NotFound() throws Exception {
        String accountId = "acc123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        doThrow(ex).when(restTemplate).delete(anyString());
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/accounts/" + accountId));

        assertThrows(NotFoundException.class, () -> customerServiceClient.removeAccount(accountId));
    }

    @Test
    void testGetAccounts_HttpClientError() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(ex);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () -> customerServiceClient.getAccounts());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }

    @Test
    void testGetAllAccounts_EmptyList() {
        ResponseEntity<List<LibraryAccountResponseModel>> response = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryAccountResponseModel> result = customerServiceClient.getAccounts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
