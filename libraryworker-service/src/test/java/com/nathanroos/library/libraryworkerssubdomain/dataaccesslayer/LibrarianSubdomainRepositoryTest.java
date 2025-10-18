package com.nathanroos.library.libraryworkerssubdomain.dataaccesslayer;

import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.Librarian;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianIdentifier;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianRepository;
import com.nathanroos.library.libraryworkerssubdomain.LibraryworkerServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = LibraryworkerServiceApplication.class)
public class LibrarianSubdomainRepositoryTest {

    @Autowired
    private LibrarianRepository repository;

    private Librarian librarian1;
    private Librarian librarian2;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        librarian1 = new Librarian();
        librarian1.setFirstname("Elena");
        librarian1.setLastname("Marquez");
        librarian1.setEmail("elena@example.com");
        librarian1.setLibrarianIdentifier(new LibrarianIdentifier());

        librarian2 = new Librarian();
        librarian2.setFirstname("Henry");
        librarian2.setLastname("Ford");
        librarian2.setEmail("henry@example.com");
        librarian2.setLibrarianIdentifier(new LibrarianIdentifier());

        repository.save(librarian1);
        repository.save(librarian2);
    }


    @Test
    void testFindById() {
        Optional<Librarian> result = repository.findById(librarian1.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("elena@example.com");
    }

    @Test
    void testFindAll() {
        List<Librarian> librarians = repository.findAll();
        assertThat(librarians).hasSize(2);
    }

    @Test
    void testSaveLibrarian() {
        Librarian librarian = new Librarian();
        librarian.setFirstname("Sarah");
        librarian.setLastname("Connor");
        librarian.setEmail("sarah@example.com");

        Librarian saved = repository.save(librarian);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("sarah@example.com");
    }

    @Test
    void testDeleteLibrarian() {
        repository.delete(librarian2);
        assertThat(repository.findById(librarian2.getId())).isEmpty();
    }

    @Test
    void testFindAllByLibrarianIdentifier_LibrarianId() {
        String librarianId = librarian1.getLibrarianIdentifier().getLibrarianId();
        Librarian result = repository.findAllByLibrarianIdentifier_LibrarianId(librarianId);
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("elena@example.com");
    }

    @Test
    void testFindAllByLibrarianIdentifier_LibrarianIdNotFound() {
        Librarian result = repository.findAllByLibrarianIdentifier_LibrarianId("non-existent-id");
        assertThat(result).isNull();
    }

    @Test
    void testSaveLibrarianWithSameEmail() {
        Librarian duplicate = new Librarian();
        duplicate.setFirstname("Duplicate");
        duplicate.setLastname("Librarian");
        duplicate.setEmail("elena@example.com"); // Same email as librarian1

        Librarian saved = repository.save(duplicate);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("elena@example.com");
    }

    @Test
    void testDeleteAllLibrarians() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void testUpdateLibrarian() {
        librarian1.setLastname("Updated");
        Librarian updated = repository.save(librarian1);

        Optional<Librarian> result = repository.findById(updated.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getLastname()).isEqualTo("Updated");
    }


}
