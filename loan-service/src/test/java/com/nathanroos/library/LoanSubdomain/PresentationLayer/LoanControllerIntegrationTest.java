
package com.nathanroos.library.LoanSubdomain.PresentationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.Loan;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanIdentifier;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanRepository;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class LoanControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private CustomerServiceClient customerServiceClient;
    @MockitoBean
    private LibraryServiceClient libraryServiceClient;
    @MockitoBean
    private LibrarianServiceClient librarianServiceClient;


    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private LoanRepository loanRepository;

    private final String BASE_URI_LOANS = "/api/v1/accounts/{accountId}/loans";

    private final String EXISTING_ACCOUNT_ID = "84a8ec6e-2fdc-4c6d-940f-9b2274d44420";
    private final String EXISTING_LOAN_ID = "1";
    private final String EXISTING_BOOK_ID = "550e8400-e29b-41d4-a716-446655440000";
    private final String EXISTING_LIBRARIAN_ID = "1a2b3c4d-e29b-41d4-a716-446655440000";

    @BeforeEach
    public void setUp() {
        loanRepository.deleteAll();

        CustomerModel customer = CustomerModel.builder().accountId(EXISTING_ACCOUNT_ID).build();
        LibraryModel book = LibraryModel.builder().bookId(EXISTING_BOOK_ID).build();
        LibraryWorkerModel librarian = LibraryWorkerModel.builder().librarianId(EXISTING_LIBRARIAN_ID).build();

        Loan existingLoan = Loan.builder()
                .id(EXISTING_LOAN_ID)
                .loanIdentifier(new LoanIdentifier(EXISTING_LOAN_ID))
                .libraryAccountIdentifier(customer)
                .bookIdentifier(book)
                .librarianIdentifier(librarian)
                .loanStatus(LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        when(customerServiceClient.getAccountByAccountId(EXISTING_ACCOUNT_ID)).thenReturn(customer);
        when(libraryServiceClient.getBookByBookId(EXISTING_BOOK_ID)).thenReturn(book);
        when(librarianServiceClient.getLibrarian(EXISTING_LIBRARIAN_ID)).thenReturn(librarian);

        loanRepository.save(existingLoan);
    }

    @Test
    public void whenLoanDoesNotExistForCustomer_thenReturnNotFound() {
        String url = BASE_URI_LOANS.replace("{accountId}", EXISTING_ACCOUNT_ID) + "/{loanId}";
        ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.GET, null, String.class, EXISTING_ACCOUNT_ID, "NON_EXISTENT_LOAN_ID");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }



    @Test
    public void whenUpdateNonexistentLoan_thenReturnNotFound() {
        LoanRequestModel updateRequest = LoanRequestModel.builder()
                .loanStatus(LoanStatusEnum.COMPLETED)
                .loanDate(new Date())
                .dueDate(new Date())
                .accountId(EXISTING_ACCOUNT_ID)
                .bookId(EXISTING_BOOK_ID)
                .librarianId(EXISTING_LIBRARIAN_ID)
                .build();

        String url = BASE_URI_LOANS.replace("{accountId}", EXISTING_ACCOUNT_ID) + "/{loanId}";
        testRestTemplate.put(url, updateRequest, EXISTING_ACCOUNT_ID, "NON_EXISTENT_LOAN_ID");

        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class, EXISTING_ACCOUNT_ID, "NON_EXISTENT_LOAN_ID");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void whenDeleteLoan_thenReturnNoContent() {
        String url = BASE_URI_LOANS.replace("{accountId}", EXISTING_ACCOUNT_ID) + "/{loanId}";
        testRestTemplate.delete(url, EXISTING_ACCOUNT_ID, EXISTING_LOAN_ID);

        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class, EXISTING_ACCOUNT_ID, EXISTING_LOAN_ID);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void whenDeleteNonexistentLoan_thenReturnNotFound() {
        String url = BASE_URI_LOANS.replace("{accountId}", EXISTING_ACCOUNT_ID) + "/{loanId}";
        testRestTemplate.delete(url, EXISTING_ACCOUNT_ID, "NON_EXISTENT_LOAN_ID");

        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class, EXISTING_ACCOUNT_ID, "NON_EXISTENT_LOAN_ID");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void whenCreateLoanWithValidData_thenReturnCreated() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId(EXISTING_ACCOUNT_ID)
                .bookId(EXISTING_BOOK_ID)
                .librarianId(EXISTING_LIBRARIAN_ID)
                .loanStatus(LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        String url = BASE_URI_LOANS.replace("{accountId}", EXISTING_ACCOUNT_ID);
        ResponseEntity<LoanResponseModel> response = testRestTemplate.postForEntity(url, request, LoanResponseModel.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(EXISTING_ACCOUNT_ID, response.getBody().getAccountId());
    }

    @Test
    public void whenCreateLoanWithMissingFields_thenReturnUnprocessableEntity() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId(EXISTING_ACCOUNT_ID)
                // Missing bookId and librarianId
                .loanStatus(LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        String url = BASE_URI_LOANS.replace("{accountId}", EXISTING_ACCOUNT_ID);
        ResponseEntity<String> response = testRestTemplate.postForEntity(url, request, String.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing required fields"));
    }

    @Test
    public void whenGetAllLoansForCustomer_thenReturnList() {
        String url = BASE_URI_LOANS.replace("{accountId}", EXISTING_ACCOUNT_ID);
        ResponseEntity<LoanResponseModel[]> response = testRestTemplate.getForEntity(url, LoanResponseModel[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 1);
    }

//    @Test
//    public void whenGetLoanWithMismatchedAccountId_thenReturnNotFound() {
//        String mismatchedAccountId = "invalid-account";
//        String url = BASE_URI_LOANS.replace("{accountId}", mismatchedAccountId) + "/{loanId}";
//        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class, mismatchedAccountId, EXISTING_LOAN_ID);
//
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        assertTrue(response.getBody().contains("not found") || response.getBody().contains("Customer not found"));
//    }



}
