package com.nathanroos.library.librarySubdomain.DataAccessLayer;

import com.nathanroos.library.librarySubdomain.dataaccesslayer.Book;
import com.nathanroos.library.librarySubdomain.dataaccesslayer.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private BookRepository repository;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        book1 = new Book();
        book1.setTitle("1984");
        book1.setAuthor("George Orwell");

        book2 = new Book();
        book2.setTitle("Brave New World");
        book2.setAuthor("Aldous Huxley");

        repository.save(book1);
        repository.save(book2);
    }

    @Test
    void testFindById() {
        Optional<Book> result = repository.findById(book1.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getAuthor()).isEqualTo("George Orwell");
    }

    @Test
    void testFindAll() {
        List<Book> books = repository.findAll();
        assertThat(books).hasSize(2);
    }

    @Test
    void testSaveBook() {
        Book book = new Book();
        book.setTitle("Dune");
        book.setAuthor("Frank Herbert");

        Book saved = repository.save(book);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Dune");
    }

    @Test
    void testDeleteBook() {
        repository.delete(book1);
        assertThat(repository.findById(book1.getId())).isEmpty();
    }

    @Test
    void testExistsById_shouldReturnTrue() {
        boolean exists = repository.existsById(book1.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsById_shouldReturnFalse() {
        boolean exists = repository.existsById(999);
        assertThat(exists).isFalse();
    }

    @Test
    void testCount_shouldReturnCorrectNumber() {
        long count = repository.count();
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testUpdateBookTitle() {
        book1.setTitle("Animal Farm");
        Book updated = repository.save(book1);
        assertThat(updated.getTitle()).isEqualTo("Animal Farm");
    }

    @Test
    void testDeleteAll_shouldEmptyRepository() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void testFindByAuthor_shouldReturnCorrectBook() {
        List<Book> found = repository.findAll()
                .stream()
                .filter(book -> "George Orwell".equals(book.getAuthor()))
                .toList();
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("1984");
    }

    @Test
    void testSaveNullTitle_shouldAllow() {
        Book book = new Book();
        book.setAuthor("Unknown");
        Book saved = repository.save(book);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isNull();
    }

    @Test
    void testSaveNullAuthor_shouldAllow() {
        Book book = new Book();
        book.setTitle("Mystery Book");
        Book saved = repository.save(book);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAuthor()).isNull();
    }

    @Test
    void testSaveAndFlush_shouldPersistImmediately() {
        Book book = new Book();
        book.setTitle("Fahrenheit 451");
        book.setAuthor("Ray Bradbury");
        Book saved = repository.saveAndFlush(book);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testFindAllEmptyAfterDeleteAll() {
        repository.deleteAll();
        List<Book> result = repository.findAll();
        assertThat(result).isEmpty();
    }

}
