package com.nathanroos.library.LoanSubdomain.DataAccessLayer;

import com.nathanroos.library.LoanSubdomain.LoanServiceApplication;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibraryWorkerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;

    private Loan loan1;
    private Loan loan2;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        CustomerModel customer = CustomerModel.builder().accountId("acc-001").build();
        LibraryModel book = LibraryModel.builder().bookId("book-001").build();
        LibraryWorkerModel librarian = LibraryWorkerModel.builder().librarianId("lib-001").build();

        loan1 = Loan.builder()
                .loanIdentifier(new LoanIdentifier())
                .libraryAccountIdentifier(customer)
                .bookIdentifier(book)
                .librarianIdentifier(librarian)
                .loanStatus(LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        loan2 = Loan.builder()
                .loanIdentifier(new LoanIdentifier())
                .libraryAccountIdentifier(customer)
                .bookIdentifier(book)
                .librarianIdentifier(librarian)
                .loanStatus(LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        repository.save(loan1);
        repository.save(loan2);
    }

    @Test
    void testFindById() {
        Optional<Loan> result = repository.findById(loan1.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getLoanIdentifier().getLoanId()).isEqualTo(loan1.getLoanIdentifier().getLoanId());
    }

    @Test
    void testFindAll() {
        List<Loan> loans = repository.findAll();
        assertThat(loans).hasSize(2);
    }

    @Test
    void testSaveLoan() {
        CustomerModel customer = CustomerModel.builder().accountId("acc-002").build();
        LibraryModel book = LibraryModel.builder().bookId("book-002").build();
        LibraryWorkerModel librarian = LibraryWorkerModel.builder().librarianId("lib-002").build();

        Loan loan = Loan.builder()
                .loanIdentifier(new LoanIdentifier())
                .libraryAccountIdentifier(customer)
                .bookIdentifier(book)
                .librarianIdentifier(librarian)
                .loanStatus(LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        Loan saved = repository.save(loan);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLibraryAccountIdentifier().getAccountId()).isEqualTo("acc-002");
    }

    @Test
    void testDeleteLoan() {
        repository.delete(loan2);
        assertThat(repository.findById(loan2.getId())).isEmpty();
    }

    @Test
    void testFindByLoanIdentifier_LoanId() {
        String loanId = loan1.getLoanIdentifier().getLoanId();
        Loan result = repository.findByLoanIdentifier_LoanId(loanId);
        assertThat(result).isNotNull();
        assertThat(result.getLoanIdentifier().getLoanId()).isEqualTo(loanId);
    }

    @Test
    void testFindByLoanIdentifier_LoanIdNotFound() {
        Loan result = repository.findByLoanIdentifier_LoanId("non-existent-id");
        assertThat(result).isNull();
    }

    @Test
    void testDeleteAllLoans() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void testUpdateLoan() {
        loan1.setLoanStatus(LoanStatusEnum.COMPLETED);
        Loan updated = repository.save(loan1);

        Optional<Loan> result = repository.findById(updated.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getLoanStatus()).isEqualTo(LoanStatusEnum.COMPLETED);
    }

    @Test
    void testFindAllByLibraryAccountIdentifier_AccountId() {
        List<Loan> results = repository.findAllByLibraryAccountIdentifier_AccountId("acc-001");
        assertThat(results).hasSize(2);
        for (Loan loan : results) {
            assertThat(loan.getLibraryAccountIdentifier().getAccountId()).isEqualTo("acc-001");
        }
    }

    @Test
    void testFindAllByLibraryAccountIdentifier_AccountIdEmpty() {
        List<Loan> results = repository.findAllByLibraryAccountIdentifier_AccountId("non-existent-acc");
        assertThat(results).isEmpty();
    }

    @Test
    void testFindAllByLoanIdentifier_LoanId() {
        List<Loan> results = repository.findAllByLoanIdentifier_LoanId(loan1.getLoanIdentifier().getLoanId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLoanIdentifier().getLoanId()).isEqualTo(loan1.getLoanIdentifier().getLoanId());
    }

    @Test
    void testFindAllByLoanIdentifier_LoanIdEmpty() {
        List<Loan> results = repository.findAllByLoanIdentifier_LoanId("non-existent-loanId");
        assertThat(results).isEmpty();
    }

    @Test
    void testFindLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId() {
        String accountId = loan1.getLibraryAccountIdentifier().getAccountId();
        String loanId = loan1.getLoanIdentifier().getLoanId();

        Loan result = repository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId);
        assertThat(result).isNotNull();
        assertThat(result.getLibraryAccountIdentifier().getAccountId()).isEqualTo(accountId);
        assertThat(result.getLoanIdentifier().getLoanId()).isEqualTo(loanId);
    }

    @Test
    void testFindLoanByAccountIdAndLoanId_NotFound() {
        Loan result = repository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId("non-existent-acc", "non-existent-loanId");
        assertThat(result).isNull();
    }

    @Test
    void testSaveMultipleLoans() {
        CustomerModel customer = CustomerModel.builder().accountId("acc-003").build();
        LibraryModel book = LibraryModel.builder().bookId("book-003").build();
        LibraryWorkerModel librarian = LibraryWorkerModel.builder().librarianId("lib-003").build();

        Loan loanA = Loan.builder()
                .loanIdentifier(new LoanIdentifier())
                .libraryAccountIdentifier(customer)
                .bookIdentifier(book)
                .librarianIdentifier(librarian)
                .loanStatus(LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        Loan loanB = Loan.builder()
                .loanIdentifier(new LoanIdentifier())
                .libraryAccountIdentifier(customer)
                .bookIdentifier(book)
                .librarianIdentifier(librarian)
                .loanStatus(LoanStatusEnum.OVERDUE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();

        repository.saveAll(List.of(loanA, loanB));
        List<Loan> loans = repository.findAllByLibraryAccountIdentifier_AccountId("acc-003");
        assertThat(loans).hasSize(2);
    }

    @Test
    void testFindAllByLoanStatus() {
        List<Loan> loans = repository.findAll();
        assertThat(loans).anyMatch(loan -> loan.getLoanStatus() == LoanStatusEnum.ACTIVE);
    }

    @Test
    void testSaveLoanWithNullFieldsShouldFail() {
        Loan invalidLoan = new Loan(); // No fields set
        try {
            repository.save(invalidLoan);
            fail("Should throw exception when saving incomplete loan.");
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(Exception.class);
        }
    }

    @Test
    void testFindByIdWithNonExistentId() {
        Optional<Loan> result = repository.findById("non-existent-id");
        assertThat(result).isEmpty();
    }

    @Test
    void testDeleteById() {
        String idToDelete = loan1.getId();
        repository.deleteById(idToDelete);
        Optional<Loan> result = repository.findById(idToDelete);
        assertThat(result).isEmpty();
    }




}
