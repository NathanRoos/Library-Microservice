package com.nathanroos.library.LoanSubdomain.BusinessLayer;

import com.nathanroos.library.LoanSubdomain.DataAccessLayer.*;
import com.nathanroos.library.LoanSubdomain.PresentationLayer.*;
import com.nathanroos.library.LoanSubdomain.MapingLayer.*;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibrarianServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibraryWorkerModel;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration")
class LoanServiceUnitTest {

    @Autowired
    private LoanService loanService;


    @MockitoBean
    private LoanRepository loanRepository;

    @MockitoBean
    private CustomerServiceClient customerServiceClient;

    @MockitoBean
    private LibraryServiceClient libraryServiceClient;

    @MockitoBean
    private LibrarianServiceClient librarianServiceClient;

    @Autowired
    private LoanRequestMapper loanRequestMapper;

    @Autowired
    private LoanResponseMapper loanResponseMapper;

    private LibraryModel libraryModel;
    private LibraryWorkerModel libraryWorkerModel;
    private CustomerModel customerModel;
    private LoanRequestModel loan1;


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
    }


    @Test
    void testLoanRequestToModel() {
        String loanId = "LOAN-123";

        Loan loan = loanRequestMapper.requestModelToEntity(loan1,new LoanIdentifier(loanId),customerModel,libraryWorkerModel,libraryModel);

        assertNotNull(loan);
        assertNotNull(loan1);
        assertNotNull(loan1.getAccountId());
        assertNotNull(loan1.getBookId());
        assertNotNull(loan1.getLibrarianId());

        assertEquals(loan.getLibraryAccountIdentifier().getAccountId(), loan1.getAccountId());
        assertEquals(loan.getBookIdentifier().getBookId(), loan1.getBookId());
        assertEquals(loan.getLibrarianIdentifier().getLibrarianId(), loan1.getLibrarianId());

    }

    @Test
    void testRequestModelToEntity_withNullLoanId() {
        Loan loan = loanRequestMapper.requestModelToEntity(
                loan1,
                new LoanIdentifier(null),
                customerModel,
                libraryWorkerModel,
                libraryModel
        );

        assertNotNull(loan);
        assertNull(loan.getLoanIdentifier().getLoanId());
        assertEquals(customerModel.getAccountId(), loan.getLibraryAccountIdentifier().getAccountId());
        assertEquals(libraryWorkerModel.getLibrarianId(), loan.getLibrarianIdentifier().getLibrarianId());
        assertEquals(libraryModel.getBookId(), loan.getBookIdentifier().getBookId());
    }

    @Test
    void testRequestModelToEntity_withPartialNullRequestModel() {
        LoanRequestModel partialRequest = LoanRequestModel.builder()
                .accountId(null)
                .bookId(libraryModel.getBookId())
                .librarianId(null)
                .build();

        Loan loan = loanRequestMapper.requestModelToEntity(
                partialRequest,
                new LoanIdentifier("LOAN-001"),
                customerModel,
                libraryWorkerModel,
                libraryModel
        );

        assertNotNull(loan);
        assertEquals("LOAN-001", loan.getLoanIdentifier().getLoanId());
        assertEquals(customerModel.getAccountId(), loan.getLibraryAccountIdentifier().getAccountId());
        assertEquals(libraryModel.getBookId(), loan.getBookIdentifier().getBookId());
        assertEquals(libraryWorkerModel.getLibrarianId(), loan.getLibrarianIdentifier().getLibrarianId());
    }

    @Test
    void testRequestModelToEntity_fieldConsistency() {
        String loanId = "LOAN-ABC-001";

        Loan loan = loanRequestMapper.requestModelToEntity(
                loan1,
                new LoanIdentifier(loanId),
                customerModel,
                libraryWorkerModel,
                libraryModel
        );

        assertAll(
                () -> assertEquals(loanId, loan.getLoanIdentifier().getLoanId()),
                () -> assertEquals(customerModel.getAccountId(), loan.getLibraryAccountIdentifier().getAccountId()),
                () -> assertEquals(libraryModel.getBookId(), loan.getBookIdentifier().getBookId()),
                () -> assertEquals(libraryWorkerModel.getLibrarianId(), loan.getLibrarianIdentifier().getLibrarianId())
        );
    }

    @Test
    void testEntityToResponseModel_includesLoanStatus() {
        Loan loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier("loan-999"))
                .libraryAccountIdentifier(customerModel)
                .bookIdentifier(libraryModel)
                .librarianIdentifier(libraryWorkerModel)
                .loanDate(new Date())
                .dueDate(new Date())
                .loanStatus(LoanStatusEnum.OVERDUE)
                .build();

        LoanResponseModel response = loanResponseMapper.entityToResponseModel(loan);

        assertNotNull(response);

    }

    @Test
    void whenGetAllLoansByAccountWithNullCustomer_thenThrowNotFoundException() {
        String accountId = "unknown-account";
        when(customerServiceClient.getAccountByAccountId(accountId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            loanService.getAllLoansByAccountId(accountId);
        });
    }

    @Test
    void whenDeleteLoan_thenDeleteCalledWithCorrectLoan() {
        String accountId = customerModel.getAccountId();
        String loanId = "550e8400-e29b-41d4-a716-446655440000";  // Use a proper UUID

        var loanToDelete = Loan.builder()
                .loanIdentifier(new LoanIdentifier(loanId))
                .libraryAccountIdentifier(customerModel)
                .build();

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId))
                .thenReturn(loanToDelete);

        loanService.removeLoan(accountId, loanId);

        verify(loanRepository).delete(loanToDelete);
    }


    @Test
    void whenSaveLoanFails_thenExceptionPropagated() {
        var loanRequest = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(any())).thenReturn(libraryWorkerModel);
        when(loanRepository.save(any(Loan.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            loanService.addLoan(loanRequest, customerModel.getAccountId());
        });
    }

    @Test
    void whenLibraryServiceFails_thenThrowsRuntimeException() {
        var loanRequest = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenThrow(new RuntimeException("Library service failure"));

        assertThrows(RuntimeException.class, () -> {
            loanService.addLoan(loanRequest, customerModel.getAccountId());
        });
    }

    @Test
    void whenLibrarianServiceFails_thenThrowsRuntimeException() {
        var loanRequest = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(any())).thenThrow(new RuntimeException("Librarian service failure"));

        assertThrows(RuntimeException.class, () -> {
            loanService.addLoan(loanRequest, customerModel.getAccountId());
        });
    }

    @Test
    void whenRepositoryReturnsNullList_thenHandleGracefully() {
        String accountId = customerModel.getAccountId();

        when(customerServiceClient.getAccountByAccountId(accountId)).thenReturn(customerModel);
        when(loanRepository.findAllByLibraryAccountIdentifier_AccountId(accountId)).thenReturn(null);

        var result = loanService.getAllLoansByAccountId(accountId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenAddLoanWithEmptyAccountId_thenThrowsInvalidInputException() {
        var loanRequest = LoanRequestModel.builder()
                .accountId("")
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        assertThrows(InvalidInputException.class, () -> {
            loanService.addLoan(loanRequest, "");
        });
    }


    @Test
    void whenAddLoanWithAllNullFields_thenThrowsInvalidInputException() {
        var loanRequest = LoanRequestModel.builder()
                .accountId(null)
                .bookId(null)
                .librarianId(null)
                .build();

        assertThrows(InvalidInputException.class, () -> {
            loanService.addLoan(loanRequest, null);
        });
    }










    @Test
    void whenDeleteLoanExists_thenDeleteSuccessfully() {
        String accountId = UUID.randomUUID().toString();
        String loanId = UUID.randomUUID().toString();

        var loanToDelete = Loan.builder()
                .loanIdentifier(new LoanIdentifier(loanId))
                .libraryAccountIdentifier(CustomerModel.builder().accountId(accountId).build())
                .build();

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId))
                .thenReturn(loanToDelete);

        loanService.removeLoan(accountId, loanId);

        verify(loanRepository).findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId);
        verify(loanRepository).delete(eq(loanToDelete));
    }


    @Test
    void whenDeleteLoanDoesNotExist_thenThrowNotFoundException() {
        String accountId = UUID.randomUUID().toString();
        String loanId = UUID.randomUUID().toString();

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId))
                .thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            loanService.removeLoan(accountId, loanId);
        });

        verify(loanRepository).findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId);
        verify(loanRepository, never()).delete(any());
    }


    @Test
    void whenGetLoanByIdDoesNotExist_thenThrowNotFoundException() {
        String accountId = UUID.randomUUID().toString();
        String loanId = "NOT-FOUND";

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId))
                .thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            loanService.getLoanByLoanId(accountId, loanId);
        });

        verify(loanRepository).findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId);
    }

    @Test
    void whenUpdateLoanWithEmptyFields_thenThrowsInvalidInputException() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("") // Empty string
                .bookId("")
                .librarianId("")
                .build();

        assertThrows(InvalidInputException.class, () -> loanService.updateLoan("", request, ""));
    }

    @Test
    void whenAddLoanWithMissingCustomer_thenThrowsNotFoundException() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("acc-1")
                .bookId("book-1")
                .librarianId("lib-1")
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> loanService.addLoan(request, "acc-1"));
    }

    @Test
    void whenAddLoanWithMissingBook_thenThrowsNotFoundException() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("acc-1")
                .bookId("book-1")
                .librarianId("lib-1")
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> loanService.addLoan(request, "acc-1"));
    }

    @Test
    void whenAddLoanWithMissingLibrarian_thenThrowsNotFoundException() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("acc-1")
                .bookId("book-1")
                .librarianId("lib-1")
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(any())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> loanService.addLoan(request, "acc-1"));
    }

    @Test
    void whenUpdateLoan_LibraryServiceFails_thenThrowsRuntimeException() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("acc-1")
                .bookId("book-1")
                .librarianId("lib-1")
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenThrow(new RuntimeException("Library failure"));

        assertThrows(RuntimeException.class, () -> loanService.updateLoan("acc-1", request, "loan-1"));
    }

    @Test
    void whenUpdateLoanWithNonexistentLoan_thenThrowsNotFoundException() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("acc-1")
                .bookId("book-1")
                .librarianId("lib-1")
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(any())).thenReturn(libraryWorkerModel);
        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(any(), any()))
                .thenReturn(null);

        String validLoanId = UUID.randomUUID().toString();  // 🔥 Use a valid UUID
        assertThrows(NotFoundException.class, () -> loanService.updateLoan("acc-1", request, validLoanId));
    }

    @Test
    void whenUpdateLoanWithValidData_thenUpdateSuccessfully() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("acc-1")
                .bookId("book-1")
                .librarianId("lib-1")
                .loanDate(new Date())
                .dueDate(new Date())
                .loanStatus(LoanStatusEnum.ACTIVE)
                .build();

        String validLoanId = "550e8400-e29b-41d4-a716-446655440000";  // ✅ Proper UUID format
        Loan loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier(validLoanId))
                .libraryAccountIdentifier(customerModel)
                .bookIdentifier(libraryModel)
                .librarianIdentifier(libraryWorkerModel)
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(any())).thenReturn(libraryWorkerModel);
        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(any(), any()))
                .thenReturn(loan);
        when(loanRepository.save(any())).thenReturn(loan);

        LoanResponseModel result = loanService.updateLoan("acc-1", request, validLoanId);  // Pass validLoanId here

        assertNotNull(result);
    }

    @Test
    void whenUpdateLoanWithEmptyAccountId_thenThrowsInvalidInputException() {
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("")
                .bookId("book-1")
                .librarianId("lib-1")
                .build();

        assertThrows(InvalidInputException.class, () -> loanService.updateLoan("", request, UUID.randomUUID().toString()));
    }

    @Test
    void whenUpdateLoanWithMissingCustomer_thenThrowsNotFoundException() {
        String loanId = UUID.randomUUID().toString();
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("acc-1")
                .bookId("book-1")
                .librarianId("lib-1")
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(null);  // Missing customer

        assertThrows(NotFoundException.class, () -> loanService.updateLoan("acc-1", request, loanId));
    }

    @Test
    void whenGetAllLoansByAccountId_NullId_thenThrowsInvalidInput() {
        assertThrows(InvalidInputException.class, () -> loanService.getAllLoansByAccountId(null));
    }

    @Test
    void whenRemoveLoanDeleteThrowsException_thenPropagate() {
        String accountId = customerModel.getAccountId();
        String loanId = "LOAN-ERROR";

        Loan loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier(loanId))
                .libraryAccountIdentifier(customerModel)
                .build();

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId))
                .thenReturn(loan);

        doThrow(new RuntimeException("Delete error")).when(loanRepository).delete(loan);

        assertThrows(RuntimeException.class, () -> loanService.removeLoan(accountId, loanId));
    }

    @Test
    void whenUpdateLoanWithDifferentStatuses_thenHandlesCorrectly() {
        String validLoanId = "550e8400-e29b-41d4-a716-446655440000";

        for (LoanStatusEnum status : LoanStatusEnum.values()) {
            LoanRequestModel request = LoanRequestModel.builder()
                    .accountId(customerModel.getAccountId())
                    .bookId(libraryModel.getBookId())
                    .librarianId(libraryWorkerModel.getLibrarianId())
                    .loanStatus(status)
                    .build();

            Loan existingLoan = Loan.builder()
                    .loanIdentifier(new LoanIdentifier(validLoanId))
                    .libraryAccountIdentifier(customerModel)
                    .bookIdentifier(libraryModel)
                    .librarianIdentifier(libraryWorkerModel)
                    .build();

            when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
            when(libraryServiceClient.getBookByBookId(any())).thenReturn(libraryModel);
            when(librarianServiceClient.getLibrarian(any())).thenReturn(libraryWorkerModel);
            when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(any(), any()))
                    .thenReturn(existingLoan);
            when(loanRepository.save(any())).thenReturn(existingLoan);

            LoanResponseModel result = loanService.updateLoan(customerModel.getAccountId(), request, validLoanId);

            assertNotNull(result);
            assertEquals(status, result.getLoanStatus());
        }
    }

    @Test
    void whenGetAllLoanByLoanIdExists_thenReturnList() {
        String loanId = UUID.randomUUID().toString();

        var loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier(loanId))
                .libraryAccountIdentifier(customerModel)
                .bookIdentifier(libraryModel)
                .librarianIdentifier(libraryWorkerModel)
                .build();

        when(loanRepository.findAllByLoanIdentifier_LoanId(loanId)).thenReturn(List.of(loan));
        when(customerServiceClient.getAccountByAccountId(anyString())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(anyString())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(anyString())).thenReturn(libraryWorkerModel);

        // 🔥 Corrected service method call!
        var result = loanService.getAllLoansByAccountId(loanId);

        assertNotNull(result);
    }


    @Test
    void whenGetAllLoanByLoanIdIsNull_thenThrowsNotFoundException() {
        String loanId = UUID.randomUUID().toString();
        when(loanRepository.findAllByLoanIdentifier_LoanId(loanId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> loanService.getAllLoansByAccountId(loanId));
    }

    @Test
    void whenRemoveLoanWithNullLoanId_thenThrowsInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> loanService.removeLoan(customerModel.getAccountId(), null));
    }

    @Test
    void whenRemoveLoanWithEmptyLoanId_thenThrowsInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> loanService.removeLoan(customerModel.getAccountId(), ""));
    }

    @Test
    void whenRemoveLoanWithIncorrectLoanIdLength_thenThrowsInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> loanService.removeLoan(customerModel.getAccountId(), "short-id"));
    }

    @Test
    void whenGetAllLoansByAccountIdWithEmptyId_thenThrowsInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> loanService.getAllLoansByAccountId("  "));
    }

    @Test
    void whenAddLoanWithMismatchedAccountId_thenThrowsInvalidInputException() {
        var request = LoanRequestModel.builder()
                .accountId("different-account")
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        assertThrows(InvalidInputException.class, () -> loanService.addLoan(request, customerModel.getAccountId()));
    }

    @Test
    void whenGetLoanByLoanIdWithNonExistentLoanId_thenThrowsNotFoundException() {
        String accountId = customerModel.getAccountId();
        String loanId = UUID.randomUUID().toString();

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId))
                .thenReturn(null);

        assertThrows(NotFoundException.class, () -> loanService.getLoanByLoanId(accountId, loanId));
    }

    @Test
    void whenUpdateLoanSaveFails_thenThrowsRuntimeException() {
        String loanId = UUID.randomUUID().toString();
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        Loan loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier(loanId))
                .libraryAccountIdentifier(customerModel)
                .bookIdentifier(libraryModel)
                .librarianIdentifier(libraryWorkerModel)
                .build();

        when(customerServiceClient.getAccountByAccountId(any())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(any())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(any())).thenReturn(libraryWorkerModel);
        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(any(), any())).thenReturn(loan);
        when(loanRepository.save(any())).thenThrow(new RuntimeException("DB save failed"));

        assertThrows(RuntimeException.class, () -> loanService.updateLoan(customerModel.getAccountId(), request, loanId));
    }

    @Test
    void whenGetAllLoansReturnsNullList_thenHandleGracefully() {
        String accountId = customerModel.getAccountId();

        when(customerServiceClient.getAccountByAccountId(accountId)).thenReturn(customerModel);
        when(loanRepository.findAllByLibraryAccountIdentifier_AccountId(accountId)).thenReturn(null);

        var result = loanService.getAllLoansByAccountId(accountId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoanRepositorySaveThrowsException_thenHandleGracefully() {
        when(customerServiceClient.getAccountByAccountId(anyString())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(anyString())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(anyString())).thenReturn(libraryWorkerModel);
        when(loanRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        LoanRequestModel request = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        assertThrows(RuntimeException.class, () -> loanService.addLoan(request, customerModel.getAccountId()));
    }

    @Test
    void whenLoanRepositoryDeleteThrowsException_thenHandleGracefully() {
        String accountId = customerModel.getAccountId();
        String loanId = UUID.randomUUID().toString();

        var loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier(loanId))
                .libraryAccountIdentifier(customerModel)
                .build();

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId))
                .thenReturn(loan);

        doThrow(new RuntimeException("Delete Error")).when(loanRepository).delete(any());

        assertThrows(RuntimeException.class, () -> loanService.removeLoan(accountId, loanId));
    }

    @Test
    void whenAddLoanWithNullRequest_thenThrowException() {
        assertThrows(InvalidInputException.class, () -> loanService.addLoan(null, customerModel.getAccountId()));
    }

    @Test
    void whenUpdateLoanWithAllDependenciesMissing_thenThrowNotFoundException() {
        String loanId = UUID.randomUUID().toString();
        var existingLoan = Loan.builder().loanIdentifier(new LoanIdentifier(loanId)).build();

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(any(), any()))
                .thenReturn(existingLoan);
        when(customerServiceClient.getAccountByAccountId(anyString())).thenReturn(null);
        when(libraryServiceClient.getBookByBookId(anyString())).thenReturn(null);
        when(librarianServiceClient.getLibrarian(anyString())).thenReturn(null);

        LoanRequestModel request = LoanRequestModel.builder()
                .accountId("missing")
                .bookId("missing")
                .librarianId("missing")
                .build();

        assertThrows(NotFoundException.class, () -> loanService.updateLoan("missing", request, loanId));
    }

    @Test
    void whenUpdateLoanRepositorySaveFails_thenThrowException() {
        String loanId = UUID.randomUUID().toString();
        var existingLoan = Loan.builder()
                .loanIdentifier(new LoanIdentifier(loanId))
                .libraryAccountIdentifier(customerModel)
                .bookIdentifier(libraryModel)
                .librarianIdentifier(libraryWorkerModel)
                .build();

        when(loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(any(), any())).thenReturn(existingLoan);
        when(customerServiceClient.getAccountByAccountId(anyString())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(anyString())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(anyString())).thenReturn(libraryWorkerModel);
        when(loanRepository.save(any())).thenThrow(new RuntimeException("Save failed"));

        LoanRequestModel request = LoanRequestModel.builder()
                .accountId(customerModel.getAccountId())
                .bookId(libraryModel.getBookId())
                .librarianId(libraryWorkerModel.getLibrarianId())
                .build();

        assertThrows(RuntimeException.class, () -> loanService.updateLoan(customerModel.getAccountId(), request, loanId));
    }

    @Test
    void whenGetAllLoansByBookIdExists_thenReturnList() {
        String bookId = UUID.randomUUID().toString();
        var loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier(UUID.randomUUID().toString()))
                .bookIdentifier(LibraryModel.builder().bookId(bookId).build())
                .libraryAccountIdentifier(customerModel)
                .librarianIdentifier(libraryWorkerModel)
                .build();

        when(loanRepository.findAllByLibraryAccountIdentifier_AccountId(bookId)).thenReturn(List.of(loan));
        when(customerServiceClient.getAccountByAccountId(anyString())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(anyString())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(anyString())).thenReturn(libraryWorkerModel);

        var result = loanService.getAllLoansByAccountId(bookId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookId, result.get(0).getBookId());
    }

    @Test
    void whenGetAllLoansByLibrarianIdExists_thenReturnList() {
        String accountId = UUID.randomUUID().toString();
        var loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier(UUID.randomUUID().toString()))
                .libraryAccountIdentifier(customerModel)
                .bookIdentifier(libraryModel)
                .librarianIdentifier(LibraryWorkerModel.builder().librarianId(accountId).build())
                .build();

        when(loanRepository.findAllByLibraryAccountIdentifier_AccountId(accountId)).thenReturn(List.of(loan));
        when(customerServiceClient.getAccountByAccountId(anyString())).thenReturn(customerModel);
        when(libraryServiceClient.getBookByBookId(anyString())).thenReturn(libraryModel);
        when(librarianServiceClient.getLibrarian(anyString())).thenReturn(libraryWorkerModel);

        var result = loanService.getAllLoansByAccountId(accountId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(accountId, result.get(0).getLibrarianId());
    }

    @Test
    void whenAddLoanWithNullRequest_thenThrowInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> loanService.addLoan(null, customerModel.getAccountId()));
    }

    @Test
    void whenUpdateLoanWithNullRequest_thenThrowInvalidInputException() {
        String loanId = UUID.randomUUID().toString();
        assertThrows(InvalidInputException.class, () -> loanService.updateLoan(customerModel.getAccountId(), null, loanId));
    }





}
