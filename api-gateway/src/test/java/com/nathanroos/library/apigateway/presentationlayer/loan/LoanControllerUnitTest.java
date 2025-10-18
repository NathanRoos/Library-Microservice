package com.nathanroos.library.apigateway.presentationlayer.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.businesslayer.loan.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetLoanById_Success() throws Exception {
        String validLoanId = "123e4567-e89b-12d3-a456-426614174000";
        LoanResponseModel response = new LoanResponseModel(
                validLoanId, "account-1", "librarian-1", "book-1",
                "John", "Doe", "Librarian", "Smith", "Book Title", "Author",
                null, new Date(), new Date()
        );

        when(loanService.getLoanByLoanId("account-1", validLoanId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/account-1/loans/" + validLoanId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanId").value(validLoanId));
    }


    @Test
    void testGetLoanById_NotFound() throws Exception {
        String validLoanId = "123e4567-e89b-12d3-a456-426614174000"; // Valid UUID

        when(loanService.getLoanByLoanId(anyString(), anyString()))
                .thenThrow(new NotFoundException("Loan not found"));

        mockMvc.perform(get("/api/v1/accounts/account-1/loans/" + validLoanId))
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetAllLoansByAccountId() throws Exception {
        LoanResponseModel response = new LoanResponseModel(
                "loan-1", "account-1", "librarian-1", "book-1",
                "John", "Doe", "Librarian", "Smith", "Book Title", "Author",
                null, new Date(), new Date()
        );

        when(loanService.getAllLoansByAccountId("account-1")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/accounts/account-1/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanId").value("loan-1"));
    }

    @Test
    void testAddLoan_Success() throws Exception {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("account-1")
                .bookId("book-1")
                .librarianId("librarian-1")
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        LoanResponseModel response = new LoanResponseModel(
                "loan-2", "account-1", "librarian-1", "book-1",
                "John", "Doe", "Librarian", "Smith", "Book Title", "Author",
                null, new Date(), new Date()
        );

        when(loanService.addLoan(any(LoanRequestModel.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts/account-1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").value("loan-2"));
    }

    @Test
    void testAddLoan_InvalidInput() throws Exception {
        when(loanService.addLoan(any(LoanRequestModel.class), anyString()))
                .thenThrow(new InvalidInputException("Invalid input"));

        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("wrong-account")
                .build(); // Missing required fields

        mockMvc.perform(post("/api/v1/accounts/account-1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUpdateLoan_Success() throws Exception {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("account-1")
                .bookId("book-1")
                .librarianId("librarian-1")
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        LoanResponseModel response = new LoanResponseModel(
                "loan-1", "account-1", "librarian-1", "book-1",
                "UpdatedFirst", "UpdatedLast", "UpdatedLibrarian", "UpdatedSmith",
                "Updated Title", "Updated Author", null, new Date(), new Date()
        );

        when(loanService.updateLoan(anyString(), any(LoanRequestModel.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/accounts/account-1/loans/loan-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value("loan-1"));
    }

    @Test
    void testDeleteLoan() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/account-1/loans/loan-1"))
                .andExpect(status().isNoContent());
    }
}
