package com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.HttpErrorInfo;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.NotFoundException;
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

    private final String host = "localhost";
    private final String port = "8080";

    @BeforeEach
    void setup() {
        customerServiceClient = new CustomerServiceClient(restTemplate, objectMapper, host, port);
    }

    @Test
    void testGetAccountByAccountId_Success() {
        String accountId = "acc-123";
        CustomerModel mockCustomer = CustomerModel.builder()
                .accountId(accountId)
                .firstname("John")
                .lastname("Doe")
                .build();
        when(restTemplate.getForObject(anyString(), eq(CustomerModel.class)))
                .thenReturn(mockCustomer);

        CustomerModel result = customerServiceClient.getAccountByAccountId(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals("John", result.getFirstname());
    }

    @Test
    void testGetAccountByAccountId_NotFound() throws Exception {
        String accountId = "acc-404";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(CustomerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not found", "/accounts/" + accountId));

        assertThrows(NotFoundException.class, () -> customerServiceClient.getAccountByAccountId(accountId));
    }

    @Test
    void testGetAccountByAccountId_InvalidInput() throws Exception {
        String accountId = "invalid!";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(CustomerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/accounts/" + accountId));

        assertThrows(InvalidInputException.class, () -> customerServiceClient.getAccountByAccountId(accountId));
    }

    @Test
    void testGetAccountByAccountId_OtherError() {
        String accountId = "error";
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(CustomerModel.class))).thenThrow(ex);

        assertThrows(RuntimeException.class, () -> customerServiceClient.getAccountByAccountId(accountId));
    }


    @Test
    void testAddAccount_Success() {
        CustomerModel request = CustomerModel.builder().accountId("acc-123").firstname("John").lastname("Doe").build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(CustomerModel.class))).thenReturn(request);

        CustomerModel result = customerServiceClient.addAccount(request);

        assertNotNull(result);
        assertEquals(request.getAccountId(), result.getAccountId());
    }

    @Test
    void testAddAccount_InvalidInput() throws Exception {
        CustomerModel request = CustomerModel.builder().accountId("acc-123").firstname("John").lastname("Doe").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(CustomerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/accounts"));

        assertThrows(InvalidInputException.class, () -> customerServiceClient.addAccount(request));
    }

    @Test
    void testGetAccounts_Success() {
        List<CustomerModel> customers = List.of(CustomerModel.builder().accountId("acc-123").firstname("John").lastname("Doe").build());
        ResponseEntity<List<CustomerModel>> response = new ResponseEntity<>(customers, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<CustomerModel> result = customerServiceClient.getAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAccounts_HttpClientError() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () -> customerServiceClient.getAccounts());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }



    @Test
    void testGetAccountByAccountId_NullId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.getAccountByAccountId(null));
    }

    @Test
    void testGetAccountByAccountId_EmptyId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.getAccountByAccountId(""));
    }

    @Test
    void testAddAccount_NullRequest_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.addAccount(null));
    }

    @Test
    void testAddCustomer_InvalidInput() throws Exception {
        CustomerModel request = CustomerModel.builder()
                .accountId("acc-123")
                .firstname("John")
                .lastname("Doe")
                .build();

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);

        // Use doThrow or doAnswer with lenient to bypass checked exception issues
        when(restTemplate.postForObject(anyString(), eq(request), eq(CustomerModel.class)))
                .thenThrow(ex);

        // Use Mockito lenient to silence unnecessary stubbing warning, and catch the exception
        lenient().when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/accounts"));

        // Run and assert
        assertThrows(InvalidInputException.class, () -> customerServiceClient.addAccount(request));
    }


    @Test
    void testGetAccounts_EmptyList() {
        ResponseEntity<List<CustomerModel>> response = new ResponseEntity<>(List.of(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<CustomerModel> result = customerServiceClient.getAccounts();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result list should be empty");
    }

    @Test
    void testGetAccounts_NullResponseBody() {
        ResponseEntity<List<CustomerModel>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<CustomerModel> result = customerServiceClient.getAccounts();

        assertNotNull(result); // Should not be null, even if response body is null
        assertTrue(result.isEmpty(), "The result list should be empty when response body is null");
    }

    @Test
    void testAddCustomer_HttpErrorOther() {
        CustomerModel request = CustomerModel.builder()
                .accountId("acc-123")
                .firstname("John")
                .lastname("Doe")
                .build();

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_GATEWAY, "Bad Gateway", null, null, null);

        when(restTemplate.postForObject(anyString(), eq(request), eq(CustomerModel.class)))
                .thenThrow(ex);

        // No need to mock objectMapper.readValue here since it won't be reached
        assertThrows(RuntimeException.class, () -> customerServiceClient.addAccount(request));
    }

    @Test
    void testUpdateAccount_PutSuccess_ButGetReturnsNull_ShouldThrowNotFound() {
        String accountId = "cust-123";
        CustomerModel request = CustomerModel.builder().accountId(accountId).build();

        doNothing().when(restTemplate).put(anyString(), eq(request));
        when(restTemplate.getForObject(contains(accountId), eq(CustomerModel.class)))
                .thenReturn(null); // Simulate getAccountByAccountId returns null

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> customerServiceClient.updateAccount(request, accountId));
        assertEquals("Customer not found for ID: " + accountId, thrown.getMessage());
    }

    @Test
    void testUpdateAccount_BlankId_ShouldThrowInvalidInput() {
        CustomerModel request = CustomerModel.builder()
                .accountId("123").firstname("John").lastname("Doe").build();
        assertThrows(InvalidInputException.class, () -> customerServiceClient.updateAccount(request, "   "));
    }

    @Test
    void testUpdateAccount_NullRequest_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.updateAccount(null, "123"));
    }

    @Test
    void testDeleteAccount_BlankId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.removeAccount("   "));
    }

    @Test
    void testGetAccountByAccountId_RestTemplateReturnsNull_ShouldThrowNotFound() {
        String accountId = "nonexistent-id";
        when(restTemplate.getForObject(anyString(), eq(CustomerModel.class))).thenReturn(null);

        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> customerServiceClient.getAccountByAccountId(accountId));

        assertEquals("Customer not found for ID: " + accountId, thrown.getMessage());
    }

    @Test
    void testCreateAccount_NullInput_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.addAccount(null));
    }

    @Test
    void testUpdateAccount_MismatchedPathId_ShouldThrowInvalidInput() throws Exception {
        String pathId = "account-999";
        CustomerModel request = CustomerModel.builder().accountId("account-123").build();

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "ID Mismatch", null, null, null);

        doThrow(ex).when(restTemplate).put(anyString(), eq(request));
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/test", "ID mismatch error"));

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> customerServiceClient.updateAccount(request, pathId));

        assertTrue(thrown.getMessage().contains("ID mismatch error"));
    }


    @Test
    void testRemoveAccount_NullInput_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.removeAccount(null));
    }

    @Test
    void testCreateAccount_HttpError() throws Exception {
        CustomerModel request = CustomerModel.builder().accountId("dummy-id").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);

        when(restTemplate.postForObject(anyString(), eq(request), eq(CustomerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/accounts", "Invalid input"));

        assertThrows(InvalidInputException.class, () -> customerServiceClient.addAccount(request));
    }

    @Test
    void testUpdateAccount_HttpError() throws Exception {
        String accountId = "bad-id";
        CustomerModel request = CustomerModel.builder().accountId("bad-id").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.BAD_GATEWAY, "Bad Gateway", null, null, null);

        doThrow(ex).when(restTemplate).put(anyString(), eq(request));
        assertThrows(HttpClientErrorException.class, () -> customerServiceClient.updateAccount(request, accountId));
    }

//    @Test
//    void testPOJOs() {
//        Address address = new CustomerAddress("123", "Main St", "City", "A1A1A1");
//        assertEquals("123", address.getStreetNumber());
//        assertEquals("Main St", address.getStreetName());
//
//        CustomerContactInfo contactInfo = new CustomerContactInfo("email@example.com", "555-1234");
//        assertEquals("email@example.com", contactInfo.getEmail());
//    }

    @Test
    void testGetAccounts_ServerErrorWithoutJsonMessage() {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);
        assertThrows(HttpClientErrorException.class, () -> customerServiceClient.getAccounts());
    }

    @Test
    void testAddAccount_ServerErrorWithoutJsonMessage() {
        CustomerModel request = CustomerModel.builder().accountId("acc-err").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(CustomerModel.class))).thenThrow(ex);
        assertThrows(HttpClientErrorException.class, () -> customerServiceClient.addAccount(request));
    }

    @Test
    void testDeleteAccount_ServerErrorWithoutJsonMessage() {
        String accountId = "acc-err";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        doThrow(ex).when(restTemplate).delete(anyString());
        assertThrows(HttpClientErrorException.class, () -> customerServiceClient.removeAccount(accountId));
    }

    @Test
    void testHandleHttpClientException_NOT_FOUND() throws Exception {
        String accountId = "nonexistent";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(CustomerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "/test", "Not found"));
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> customerServiceClient.getAccountByAccountId(accountId));
        assertTrue(thrown.getMessage().contains("Not found"));
    }

    @Test
    void testHandleHttpClientException_UNPROCESSABLE_ENTITY() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", null, null, null);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/test", "Invalid input"));
        when(restTemplate.postForObject(anyString(), any(), eq(CustomerModel.class))).thenThrow(ex);
        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> customerServiceClient.addAccount(CustomerModel.builder().accountId("dummy").firstname("First").lastname("Last").build()));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    void testHandleHttpClientException_OtherError() {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Error", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(CustomerModel.class))).thenThrow(ex);
        assertThrows(HttpClientErrorException.class, () -> customerServiceClient.getAccountByAccountId("123"));
    }

    @Test
    void testGetAccounts_ResponseBodyNull() {
        ResponseEntity<List<CustomerModel>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);
        List<CustomerModel> result = customerServiceClient.getAccounts();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateAccount_IncompleteFields_ShouldThrowInvalidInput() throws Exception {
        CustomerModel incompleteRequest = CustomerModel.builder().firstname("John").build(); // missing fields
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(incompleteRequest), eq(CustomerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input: missing fields", "/accounts"));
        assertThrows(InvalidInputException.class, () -> customerServiceClient.addAccount(incompleteRequest));
    }

    @Test
    void testUpdateAccount_NullId_ShouldThrowInvalidInput() {
        CustomerModel request = CustomerModel.builder().accountId("123").firstname("John").lastname("Doe").build();
        assertThrows(InvalidInputException.class, () -> customerServiceClient.updateAccount(request, null));
    }

    @Test
    void testUpdateAccount_WhitespaceId_ShouldThrowInvalidInput() {
        CustomerModel request = CustomerModel.builder().accountId("123").firstname("John").lastname("Doe").build();
        assertThrows(InvalidInputException.class, () -> customerServiceClient.updateAccount(request, "   "));
    }

    @Test
    void testDeleteAccount_WhitespaceId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.removeAccount("   "));
    }

    @Test
    void testCreateAccount_EmptyFields_ShouldStillSucceed() {
        CustomerModel request = CustomerModel.builder()
                .accountId("")
                .firstname("")
                .lastname("")
                .build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(CustomerModel.class)))
                .thenReturn(request);
        CustomerModel result = customerServiceClient.addAccount(request);
        assertNotNull(result);
        assertEquals("", result.getFirstname());
    }

    @Test
    void testGetAccounts_EmptyListResponse() {
        ResponseEntity<List<CustomerModel>> response = new ResponseEntity<>(List.of(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        List<CustomerModel> result = customerServiceClient.getAccounts();
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list when no accounts found");
    }

    @Test
    void testGetAccountByAccountId_WhitespaceId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> customerServiceClient.getAccountByAccountId("   "));
    }

    @Test
    void testUpdateAccount_ServerErrorWithoutJsonMessage() {
        String accountId = "account-500";
        CustomerModel request = CustomerModel.builder().accountId(accountId).build();
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        doThrow(ex).when(restTemplate).put(anyString(), eq(request));
        assertThrows(HttpClientErrorException.class, () -> customerServiceClient.updateAccount(request, accountId));
    }

    @Test
    void testRemoveAccount_ServerErrorWithoutJsonMessage() {
        String accountId = "acc-err";
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        doThrow(ex).when(restTemplate).delete(anyString());
        assertThrows(HttpClientErrorException.class, () -> customerServiceClient.removeAccount(accountId));
    }


    @Test
    void testInvalidInputExceptionConstructors() {
        InvalidInputException ex1 = new InvalidInputException("Message");
        InvalidInputException ex2 = new InvalidInputException(new RuntimeException("Cause"));
        InvalidInputException ex3 = new InvalidInputException("Message", new RuntimeException("Cause"));
        assertNotNull(ex1);
        assertNotNull(ex2);
        assertNotNull(ex3);
    }

    @Test
    void testNotFoundExceptionConstructors() {
        NotFoundException ex1 = new NotFoundException("Message");
        NotFoundException ex2 = new NotFoundException(new RuntimeException("Cause"));
        NotFoundException ex3 = new NotFoundException("Message", new RuntimeException("Cause"));
        assertNotNull(ex1);
        assertNotNull(ex2);
        assertNotNull(ex3);
    }






}
