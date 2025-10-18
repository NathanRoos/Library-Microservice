package com.nathanroos.library.customersubdomain.DataAccessLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LibraryAccountRepositoryIntegrationTest {

    @Autowired
    private LibraryAccountRepository libraryAccountRepository;

    @BeforeEach
    public void setUp() {
        libraryAccountRepository.deleteAll();
    }

    @Test
    public void whenLibraryAccountsExist_thenReturnAllAccounts() {
        // Arrange
        LibraryAccount account1 = buildSampleLibraryAccount();
        LibraryAccount account2 = buildSampleLibraryAccount();

        libraryAccountRepository.save(account1);
        libraryAccountRepository.save(account2);

        long countAfterInsert = libraryAccountRepository.count();

        // Act
        List<LibraryAccount> foundAccounts = libraryAccountRepository.findAll();

        // Assert
        assertNotNull(foundAccounts);
        assertNotEquals(0, countAfterInsert);
        assertEquals(countAfterInsert, foundAccounts.size());
    }

    @Test
    public void whenLibraryAccountExists_thenReturnById() {
        // Arrange
        LibraryAccount account = buildSampleLibraryAccount();
        LibraryAccount saved = libraryAccountRepository.save(account);

        Integer accountId = saved.getId();

        // Act
        LibraryAccount foundAccount = libraryAccountRepository.findById(accountId).orElse(null);

        // Assert
        assertNotNull(foundAccount);
        assertEquals(account.getFirstname(), foundAccount.getFirstname());
        assertEquals(account.getLastname(), foundAccount.getLastname());
        assertEquals(account.getPhoneNumber(), foundAccount.getPhoneNumber());
        assertEquals(account.getEmail(), foundAccount.getEmail());
    }

    @Test
    public void whenLibraryAccountDoesNotExist_thenReturnNull() {
        // Act
        LibraryAccount foundAccount = libraryAccountRepository.findById(999).orElse(null);

        // Assert
        assertNull(foundAccount);
    }

    @Test
    public void whenValidLibraryAccountSaved_thenPersistAndReturnCorrectData() {
        // Arrange
        LibraryAccount account = new LibraryAccount(
                "514-123-4567",
                "Sophia",
                "Nguyen",
                "sophia.nguyen@example.com"
        );

        // Act
        LibraryAccount savedAccount = libraryAccountRepository.save(account);

        // Assert
        assertNotNull(savedAccount);
        assertNotNull(savedAccount.getId());
        assertEquals(account.getFirstname(), savedAccount.getFirstname());
        assertEquals(account.getLastname(), savedAccount.getLastname());
        assertEquals(account.getPhoneNumber(), savedAccount.getPhoneNumber());
        assertEquals(account.getEmail(), savedAccount.getEmail());
    }

    // Helper method to create sample data
    private LibraryAccount buildSampleLibraryAccount() {
        return new LibraryAccount(
                "514-555-1234",
                "Emma",
                "Smith",
                "emma.smith@example.com"
        );
    }
}
