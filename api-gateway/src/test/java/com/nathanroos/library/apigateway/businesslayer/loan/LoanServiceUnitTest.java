package com.nathanroos.library.apigateway.businesslayer.loan;

import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.domainclientlayer.loan.LoanServiceClient;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceUnitTest {

    @Mock
    private LoanServiceClient loanServiceClient;

    @InjectMocks
    private LoanServiceImpl loanService;

    private LoanResponseModel responseModel;
    private LoanRequestModel requestModel;
    private String accountId;
    private String loanId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID().toString();
        loanId = UUID.randomUUID().toString();

        responseModel = LoanResponseModel.builder()
                .loanId(loanId)
                .accountId(accountId)
                .librarianId("lib123")
                .bookId("book123")
                .customer_firstname("John")
                .customer_lastname("Doe")
                .librarian_firstname("Jane")
                .librarian_lastname("Smith")
                .title("The Great Book")
                .author("Author Name")
                .loanStatus(com.nathanroos.library.apigateway.domainclientlayer.loan.LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        requestModel = LoanRequestModel.builder()
                .accountId(accountId)
                .bookId("book123")
                .librarianId("lib123")
                .loanStatus(com.nathanroos.library.apigateway.domainclientlayer.loan.LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();
    }

    @Test
    void whenGetLoanByLoanId_thenReturnLoan() {
        when(loanServiceClient.getLoanByLoanId(accountId, loanId)).thenReturn(responseModel);

        var result = loanService.getLoanByLoanId(accountId, loanId);

        assertNotNull(result);
        assertEquals(responseModel.getLoanId(), result.getLoanId());
        assertEquals(responseModel.getAccountId(), result.getAccountId());
    }

    @Test
    void whenGetAllLoans_thenReturnList() {
        when(loanServiceClient.getLoans(accountId)).thenReturn(List.of(responseModel));

        var result = loanService.getAllLoansByAccountId(accountId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseModel.getLoanId(), result.get(0).getLoanId());
    }

    @Test
    void whenGetAllLoansIsEmpty_thenReturnEmptyList() {
        when(loanServiceClient.getLoans(accountId)).thenReturn(Collections.emptyList());

        var result = loanService.getAllLoansByAccountId(accountId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenAddLoan_thenReturnCreatedLoan() {
        when(loanServiceClient.addLoan(accountId, requestModel)).thenReturn(responseModel);

        var result = loanService.addLoan(requestModel, accountId);

        assertNotNull(result);
        assertEquals(responseModel.getLoanId(), result.getLoanId());
        assertEquals(responseModel.getAccountId(), result.getAccountId());
    }

    @Test
    void whenUpdateLoan_thenReturnUpdatedLoan() {
        when(loanServiceClient.updateLoan(accountId, requestModel, loanId)).thenReturn(responseModel);

        var result = loanService.updateLoan(accountId, requestModel, loanId);

        assertNotNull(result);
        assertEquals(responseModel.getLoanId(), result.getLoanId());
    }

    @Test
    void whenDeleteLoan_thenVerifyClientCall() {
        doNothing().when(loanServiceClient).deleteLoan(accountId, loanId);

        loanService.removeLoan(accountId, loanId);

        verify(loanServiceClient, times(1)).deleteLoan(accountId, loanId);
    }

    @Test
    void whenGetLoanByLoanIdNotFound_thenThrowNotFoundException() {
        when(loanServiceClient.getLoanByLoanId(anyString(), anyString())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            loanService.getLoanByLoanId("nonexistent-account", "nonexistent-loan");
        });
    }

    @Test
    void whenLoanServiceClientThrowsException_thenPropagate() {
        when(loanServiceClient.getLoanByLoanId(anyString(), anyString())).thenThrow(new RuntimeException("Service failure"));

        assertThrows(RuntimeException.class, () -> {
            loanService.getLoanByLoanId("some-account", "some-loan");
        });
    }
}
