package com.nathanroos.library.librarySubdomain.PresentationLayer;

import com.nathanroos.library.librarySubdomain.dataaccesslayer.BookRepository;
import com.nathanroos.library.librarySubdomain.dataaccesslayer.GenreEnum;
import com.nathanroos.library.librarySubdomain.presentationlayer.BookRequestModel;
import com.nathanroos.library.librarySubdomain.presentationlayer.BookResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BookRepository repository;

    private final String BASE_URI = "/api/v1/books";
    private final String NOT_FOUND_ID = "550e8400-e29b-41d4-a716-446655440001";
    private final String INVALID_ID = "this-is-not-a-uuid";
    private final String VALID_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    void whenBookExists_thenReturnAllBook() {
        long sizeDB = this.repository.count();

        this.webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BookResponseModel.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertNotEquals(0, sizeDB);
                    assertEquals(sizeDB, list.size());
                    list.forEach((authorResponseModel) -> {
                        assertNotNull(authorResponseModel);
                        assertNotNull(authorResponseModel.getBookId());
                        assertNotNull(authorResponseModel.getFirstname());
                        assertNotNull(authorResponseModel.getLastname());
                    });
                });
    }

    @Test
    void whenValidBookId_thenReturnOk() {
        webTestClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getBookId());
                    assertNotNull(response.getAuthor());
                    assertNotNull(response.getCopiesAvailable());
                    assertNotNull(response.getTitle());
                    assertNotNull(response.getGenre());
                    assertNotNull(response.getFirstname());
                    assertNotNull(response.getLastname());
                });
    }

    @Test
    void whenValidBook_thenReturnCreated() {
        BookRequestModel request = BookRequestModel.builder()
                .author("George Orwell")
                .title("1984")
                .firstname("George")
                .lastname("Orwell")
                .genre(GenreEnum.FICTION)
                .copiesAvailable(5)

                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getBookId());
                    assertEquals(request.getAuthor(), response.getAuthor());
                    assertEquals(request.getTitle(), response.getTitle());
                });
    }

    @Test
    void whenValidBookId_thenDeleteAccount() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown bookId: " + VALID_ID);
    }

    @Test
    void whenInvalidIdFormat_thenReturnUnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URI + "/" +INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenBookNotFound_thenReturn404() {
        webTestClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown bookId: " + NOT_FOUND_ID);
    }

    @Test
    void whenBookFirstNameIsInvalidOnPost_thenReturnBadRequest() {
        BookRequestModel bookRequestModel = BookRequestModel.builder()
                .firstname("")
                .lastname("Orwell")
                .build();

        this.webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid firstName: " + bookRequestModel.getFirstname());
    }

    @Test
    void whenLastnameIsMissing_thenReturnUnprocessableEntity() {
        BookRequestModel request = BookRequestModel.builder()
                .firstname("George")
                .genre(GenreEnum.FICTION)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenInvalidAuthor_thenReturnUnprocessableEntity() {
        BookRequestModel request = BookRequestModel.builder()
                .firstname("George")
                .lastname("Orwell")
                .author("")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenInvalidUUIDOnDelete_thenReturnUnprocessableEntity() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid bookId: " + INVALID_ID);
    }

    @Test
    void whenNonexistentUUIDOnDelete_thenReturnNotFound() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown bookId: " + NOT_FOUND_ID);
    }

    @Test
    void whenCreatingBookWithNullFields_thenReturnUnprocessableEntity() {
        BookRequestModel request = BookRequestModel.builder().build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenUpdatingValidBook_thenReturnUpdatedData() {
        BookRequestModel update = BookRequestModel.builder()
                .author("Updated Author")
                .title("Updated Title")
                .firstname("NewFirst")
                .lastname("NewLast")
                .genre(GenreEnum.FANTASY)
                .copiesAvailable(2)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookResponseModel.class)
                .value(res -> {
                    assertEquals("Updated Author", res.getAuthor());
                    assertEquals("Updated Title", res.getTitle());
                });
    }

    @Test
    void whenUpdatingNonexistentBook_thenReturnNotFound() {
        BookRequestModel update = BookRequestModel.builder()
                .author("Ghost")
                .title("Invisible Book")
                .firstname("NoOne")
                .lastname("Unknown")
                .genre(GenreEnum.FANTASY)
                .copiesAvailable(1)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenUpdatingWithInvalidId_thenReturnUnprocessableEntity() {
        BookRequestModel update = BookRequestModel.builder()
                .author("Ghost")
                .title("Invisible Book")
                .firstname("NoOne")
                .lastname("Unknown")
                .genre(GenreEnum.FANTASY)
                .copiesAvailable(1)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + INVALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenCreatingBookWithLongTitle_thenReturnCreated() {
        BookRequestModel request = BookRequestModel.builder()
                .author("Author")
                .title("A".repeat(255))
                .firstname("A")
                .lastname("B")
                .genre(GenreEnum.FANTASY)
                .copiesAvailable(1)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void whenCreatingBookWithNegativeCopies_thenReturnUnprocessableEntity() {
        BookRequestModel request = BookRequestModel.builder()
                .author("Negative Guy")
                .title("Negative Book")
                .firstname("Nope")
                .lastname("Nada")
                .genre(GenreEnum.FANTASY)
                .copiesAvailable(-1)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @Test
    void whenGetAllBooks_thenCheckResponseStructure() {
        webTestClient.get()
                .uri(BASE_URI)
                .exchange()
                .expectBodyList(BookResponseModel.class)
                .value(list -> list.forEach(book -> {
                    assertNotNull(book.getBookId());
                    assertNotNull(book.getTitle());
                    assertNotNull(book.getAuthor());
                }));
    }

    @Test
    void whenCreatingBookWithMissingGenre_thenReturnUnprocessableEntity() {
        BookRequestModel request = BookRequestModel.builder()
                .author("Anon")
                .title("Missing Genre")
                .firstname("F")
                .lastname("L")
                .copiesAvailable(1)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenCreatingDuplicateBook_thenAllowIfNoConstraint() {
        BookRequestModel request = BookRequestModel.builder()
                .author("George Orwell")
                .title("1984")
                .firstname("George")
                .lastname("Orwell")
                .genre(GenreEnum.FICTION)
                .copiesAvailable(5)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }


}
