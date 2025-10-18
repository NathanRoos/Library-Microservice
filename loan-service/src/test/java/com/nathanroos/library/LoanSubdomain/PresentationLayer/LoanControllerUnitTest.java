
package com.nathanroos.library.LoanSubdomain.PresentationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.LoanSubdomain.BusinessLayer.LoanService;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanIdentifier;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanStatusEnum;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibrarianServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibraryWorkerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoanControllerUnitTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @MockitoBean
    private LibrarianServiceClient librarianServiceClient;

    @MockitoBean
    private LibraryServiceClient libraryServiceClient;

    @MockitoBean
    private CustomerServiceClient customerServiceClient;

    private LibraryModel libraryModel;
    private LibraryWorkerModel libraryWorkerModel;
    private CustomerModel customerModel;
    private LoanRequestModel loan1;


    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/accounts";
    }


    @BeforeEach
    void setUp() {
        var loanIdentifier = new LoanIdentifier();


        libraryModel = LibraryModel.builder()
                .bookId("550e8400-e29b-41d4-a716-446655440000")
                .title("1984")
                .author("George Orwell")
                .build();
        libraryWorkerModel = LibraryWorkerModel.builder()
                .librarianId("1a2b3c4d-e29b-41d4-a716-446655440000")
                .firstname("Alice")
                .lastname("Johnson")
                .build();

        customerModel = CustomerModel.builder()
                .accountId("84a8ec6e-2fdc-4c6d-940f-9b2274d44420")
                .firstname("John")
                .lastname("Doe")
                .build();

        loan1 = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        when(customerServiceClient.getAccountByAccountId(customerModel.getAccountId()))
                .thenReturn(customerModel);

        when(librarianServiceClient.getLibrarian(libraryWorkerModel.getLibrarianId()))
                .thenReturn(libraryWorkerModel);

        when(libraryServiceClient.getBookByBookId(libraryModel.getBookId()))
                .thenReturn(libraryModel);
    }

    @Test
    void whenGetAllLoansByAccount_thenReturnOkAndList() {
        String url = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoanRequestModel> request = new HttpEntity<>(loan1, headers);

        ResponseEntity<LoanResponseModel> postResponse =
                restTemplate.postForEntity(url, request, LoanResponseModel.class);

        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        String loanId = Objects.requireNonNull(postResponse.getBody()).getLoanId();
        assertNotNull(loanId);

        ResponseEntity<LoanResponseModel[]> response =
                restTemplate.getForEntity(url, LoanResponseModel[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 1);
    }

    @Test
    void whenPostValidLoan_thenReturnCreated() {
        String url = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoanRequestModel> request = new HttpEntity<>(loan1, headers);

        ResponseEntity<LoanResponseModel> response =
                restTemplate.postForEntity(url, request, LoanResponseModel.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");

        LoanResponseModel responseBody = response.getBody();
        assertNotNull(responseBody.getTitle(), "Book title in response should not be null");
        assertEquals(libraryModel.getTitle(), responseBody.getTitle(), "Book title should match the mock data");
    }

    @Test
    void whenGetInvalidUUID_thenReturnUnprocessableEntity() {
        String accountId = customerModel.getAccountId();
        String invalidUUID = "not-a-uuid";

        try {
            String url = getBaseUrl() + "/" + accountId + "/loans/" + invalidUUID;
            restTemplate.getForEntity(url, LoanResponseModel.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode(), "Expected HTTP 422 Unprocessable Entity");
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody, "Response body should not be null");
            assertTrue(responseBody.contains("The following Id is not the correct length: " + invalidUUID), "Response body should contain the expected message");
        }
    }

    @Test
    void whenDeleteExistingLoan_thenReturnNoContent() {
        String createUrl = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoanRequestModel> request = new HttpEntity<>(loan1, headers);

        ResponseEntity<LoanResponseModel> postResponse =
                restTemplate.postForEntity(createUrl, request, LoanResponseModel.class);

        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody(), "Response body should not be null");
        String loanId = postResponse.getBody().getLoanId();
        assertNotNull(loanId, "Loan ID should not be null");

        String deleteUrl = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans/" + loanId;
        restTemplate.delete(deleteUrl);

        try {
            restTemplate.getForEntity(deleteUrl, String.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode(), "Expected HTTP 404 Not Found");
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody, "Response body should not be null");
            assertTrue(responseBody.contains("The Id is not found:" + loanId), "Response body should contain the loan not found error message");
        }
    }

    @Test
    void whenUpdateLoanWithValidData_thenReturnOk() {
        // First, create a loan to update
        String createUrl = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoanRequestModel> createRequest = new HttpEntity<>(loan1, headers);

        ResponseEntity<LoanResponseModel> postResponse =
                restTemplate.postForEntity(createUrl, createRequest, LoanResponseModel.class);
        String loanId = postResponse.getBody().getLoanId();

        // Now, update the loan with valid data
        String updateUrl = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans/" + loanId;

        LoanRequestModel updatedLoanRequest = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .loanStatus(LoanStatusEnum.OVERDUE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        HttpEntity<LoanRequestModel> updateRequest = new HttpEntity<>(updatedLoanRequest, headers);

        ResponseEntity<LoanResponseModel> response =
                restTemplate.exchange(updateUrl, HttpMethod.PUT, updateRequest, LoanResponseModel.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(LoanStatusEnum.OVERDUE, response.getBody().getLoanStatus());
    }

    @Test
    void whenUpdateLoanWithMismatchedAccount_thenReturnNotFound() {
        String createUrl = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoanRequestModel> createRequest = new HttpEntity<>(loan1, headers);

        ResponseEntity<LoanResponseModel> postResponse =
                restTemplate.postForEntity(createUrl, createRequest, LoanResponseModel.class);
        String loanId = postResponse.getBody().getLoanId();

        String wrongAccountId = "wrong-account-id";
        String updateUrl = getBaseUrl() + "/" + wrongAccountId + "/loans/" + loanId;

        LoanRequestModel updatedLoanRequest = LoanRequestModel.builder()
                .accountId(wrongAccountId)
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .loanStatus(LoanStatusEnum.OVERDUE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        HttpEntity<LoanRequestModel> updateRequest = new HttpEntity<>(updatedLoanRequest, headers);

        try {
            restTemplate.exchange(updateUrl, HttpMethod.PUT, updateRequest, LoanResponseModel.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode(), "Expected HTTP 404 Not Found");
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody, "Response body should not be null");
            assertTrue(responseBody.contains("Customer not found"), "Expected error message to contain 'Customer not found'");
        }
    }

    @Test
    void whenAddLoanWithMismatchedAccountId_thenReturnBadRequest() {
        String url = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans";

        LoanRequestModel mismatchedLoanRequest = LoanRequestModel.builder()
                .accountId("different-account-id") // Deliberately different
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoanRequestModel> request = new HttpEntity<>(mismatchedLoanRequest, headers);

        try {
            restTemplate.postForEntity(url, request, LoanResponseModel.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody);
            assertTrue(responseBody.contains("AccountId in path and body must match"), "Expected validation error message");
        }
    }

    @Test
    void whenDeleteNonExistentLoan_thenReturnNotFound() {
        String nonExistentLoanId = UUID.randomUUID().toString();
        String url = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans/" + nonExistentLoanId;

        try {
            restTemplate.delete(url);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody);
            // 🔥 Update to match your actual error message
            assertTrue(responseBody.contains("Provided loanId not found") ||
                    responseBody.contains("The Id is not found"), "Expected error message");
        }
    }

    @Test
    void whenGetLoansForNonExistentAccount_thenReturnNotFound() {
        String nonExistentAccountId = "non-existent-id";
        String url = getBaseUrl() + "/" + nonExistentAccountId + "/loans";

        try {
            restTemplate.getForEntity(url, LoanResponseModel[].class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody);
            assertTrue(responseBody.contains("Customer not found") || responseBody.contains("The Id is not found"), "Expected error message");
        }
    }

    @Test
    void whenGetLoanByInvalidUUID_thenReturnUnprocessableEntity() {
        String url = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans/invalid-uuid";

        try {
            restTemplate.getForEntity(url, LoanResponseModel.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody);
            assertTrue(responseBody.contains("Invalid") || responseBody.contains("The following Id is not the correct length"), "Expected UUID error message");
        }
    }

    @Test
    void whenGetAllLoansForAccountWithNoLoans_thenReturnEmptyList() {
        String emptyAccountId = "empty-account-id";
        when(customerServiceClient.getAccountByAccountId(emptyAccountId)).thenReturn(customerModel); // Mock account exists but no loans

        String url = getBaseUrl() + "/" + emptyAccountId + "/loans";

        ResponseEntity<LoanResponseModel[]> response = restTemplate.getForEntity(url, LoanResponseModel[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length, "Expected no loans returned");
    }

    @Test
    void whenGetLoansForAccountWithNoLoans_thenReturnEmptyList() {
        String accountId = UUID.randomUUID().toString();
        String url = getBaseUrl() + "/" + accountId + "/loans";

        // Mock the customer to exist but no loans
        when(customerServiceClient.getAccountByAccountId(accountId))
                .thenReturn(CustomerModel.builder().accountId(accountId).build());

        ResponseEntity<LoanResponseModel[]> response = restTemplate.getForEntity(url, LoanResponseModel[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length, "Expected no loans for the account");
    }

    @Test
    void whenPostLoanWithMissingFields_thenReturnUnprocessableEntity() {
        String url = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans";

        // LoanRequestModel with missing bookId and librarianId
        LoanRequestModel incompleteLoan = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoanRequestModel> request = new HttpEntity<>(incompleteLoan, headers);

        try {
            restTemplate.postForEntity(url, request, LoanResponseModel.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            // Expect HTTP 422 Unprocessable Entity
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode(), "Expected HTTP 422 Unprocessable Entity");

            // Check the message about missing fields
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody, "Response body should not be null");
            assertTrue(responseBody.contains("Missing required fields"), "Response body should indicate missing fields");
        }
    }

    @Test
    void whenGetLoanWithWrongAccountId_thenReturnNotFound() {
        String invalidAccountId = "invalid-account";
        String loanId = UUID.randomUUID().toString(); // generate a random loan ID

        String url = getBaseUrl() + "/" + invalidAccountId + "/loans/" + loanId;

        try {
            restTemplate.getForEntity(url, LoanResponseModel.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode(), "Expected HTTP 404 Not Found");
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody, "Response body should not be null");
            assertTrue(responseBody.contains("The Id is not found"), "Expected error message to contain 'The Id is not found'");
        }
    }

    @Test
    void whenUpdateLoanWithMissingFields_thenReturnUnprocessableEntity() {
        // Create a loan to update
        String createUrl = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoanRequestModel> createRequest = new HttpEntity<>(loan1, headers);
        ResponseEntity<LoanResponseModel> postResponse =
                restTemplate.postForEntity(createUrl, createRequest, LoanResponseModel.class);
        String loanId = postResponse.getBody().getLoanId();

        // Now attempt to update with missing bookId
        String updateUrl = getBaseUrl() + "/" + customerModel.getAccountId() + "/loans/" + loanId;

        LoanRequestModel updatedLoanRequest = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(null)  // Missing bookId
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        HttpEntity<LoanRequestModel> updateRequest = new HttpEntity<>(updatedLoanRequest, headers);

        try {
            restTemplate.exchange(updateUrl, HttpMethod.PUT, updateRequest, LoanResponseModel.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
            String responseBody = ex.getResponseBodyAsString();
            assertNotNull(responseBody);
            assertTrue(responseBody.contains("bookId cannot be null or empty"),
                    "Expected error message to mention missing bookId");
        }
    }


}
