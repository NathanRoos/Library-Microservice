package com.nathanroos.library.libraryworkerssubdomain.PresentationLayer;

import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianPhoneNumber;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianRepository;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.Position;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.PositionEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LibrarianControllerIntegrationTest {


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LibrarianRepository repository;

    private final String BASE_URI = "/api/v1/workers";
    private final String NOT_FOUND_ID = "1a2b3c4d-e29b-41d4-a716-446655440001";
    private final String INVALID_ID = "this-is-not-a-uuid";
    private final String VALID_ID = "1a2b3c4d-e29b-41d4-a716-446655440000";

    @Test
    void whenLibrarianExists_thenReturnAllLibrarians() {
        long sizeDB = this.repository.count();

        this.webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(LibrarianResponseModel.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertNotEquals(0, sizeDB);
                    assertEquals(sizeDB, list.size());
                    list.forEach((librarianResponseModel) -> {
                        assertNotNull(librarianResponseModel);
                        assertNotNull(librarianResponseModel.getEmail());
                        assertNotNull(librarianResponseModel.getFirstname());
                        assertNotNull(librarianResponseModel.getLastname());
                        assertNotNull(librarianResponseModel.getLibrarianPhoneNumber());
                        assertNotNull(librarianResponseModel.getPosition());
                    });
                });
    }

    @Test
    void whenValidLibrarianId_thenReturnOk() {
        webTestClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(LibrarianResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getEmail());
                    assertNotNull(response.getFirstname());
                    assertNotNull(response.getLastname());
                    assertNotNull(response.getLibrarianPhoneNumber());
                    assertNotNull(response.getPosition());
                });
    }

    @Test
    void whenValidLibrarian_thenReturnCreated() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .email("alice.johnson@example.com")
                .firstname("Alice")
                .lastname("Johnson")
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .position(new Position(PositionEnum.LIBRARY_CLERK))

                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(LibrarianResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getLibrarianId());
                    assertEquals(request.getLastname(), response.getLastname());
                    assertEquals(request.getFirstname(), response.getFirstname());
                });
    }

    @Test
    void whenValidLibrarianId_thenDeleteWorker() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown librarianId: " + VALID_ID);
    }

    @Test
    void whenInvalidIdFormat_thenReturnUnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URI + "/" +INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenLibrarianNotFound_thenReturn404() {
        webTestClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown librarianId: " + NOT_FOUND_ID);
    }

    @Test
    void whenLibrarianFirstNameIsInvalidOnPost_thenReturnUnprocessableEntity() {
        LibrarianRequestModel librarianRequestModel = LibrarianRequestModel.builder()
                .firstname("")
                .lastname("Orwell")
                .build();

        this.webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(librarianRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid firstName: " + librarianRequestModel.getFirstname());
    }

    @Test
    void whenLastnameIsMissing_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("George")
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenInvalidLibrarian_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("George")
                .lastname("Orwell")
                .position(new Position(PositionEnum.LIBRARY_CLERK))
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
                .jsonPath("$.message").isEqualTo("Invalid librarianId: " + INVALID_ID);
    }

    @Test
    void whenNonexistentUUIDOnDelete_thenReturnNotFound() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown librarianId: " + NOT_FOUND_ID);
    }

    @Test
    void whenEmailIsMissing_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("George")
                .lastname("Orwell")
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenPhoneNumberMissing_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Emily")
                .lastname("Blunt")
                .email("emily@example.com")
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }



    @Test
    void whenCreatingWithEmptyFields_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("")
                .lastname("")
                .email("")
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenPositionMissing_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("George")
                .lastname("Martin")
                .email("george@example.com")
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    ///////THIS WORKS///////




    @Test
    void whenPhoneNumberObjectIsMissing_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Robert")
                .lastname("Lane")
                .email("robert.lane@example.com")
                .position(new Position(PositionEnum.ASSISTANT))
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenEmptyEmail_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Tom")
                .lastname("Cruze")
                .email("")
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenPositionIsMissing_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Jenny")
                .lastname("Cruz")
                .email("jenny.cruz@example.com")
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }



    @Test
    void whenDeletingAlreadyDeletedLibrarian_thenReturnNotFound() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.delete()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenAllFieldsNull_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder().build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @Test
    void whenCreatingWithOnlyEmail_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .email("test@example.com")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenEmptyRequestBody_thenReturnBadRequest() {
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void whenGetAllWithNoData_thenReturnEmptyList() {
        repository.deleteAll();

        webTestClient.get()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LibrarianResponseModel.class)
                .hasSize(0);
    }



    @Test
    void whenPutWithInvalidUUID_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("jane.doe@example.com")
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .position(new Position(PositionEnum.ASSISTANT))
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenUpdatingNonexistentId_thenReturnNotFound() {
        String fakeId = "123e4567-e89b-12d3-a456-556642440999";

        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Update")
                .lastname("Test")
                .email("update@example.com")
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + fakeId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenFetchingAll_thenEachHasValidEmailField() {
        webTestClient.get()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LibrarianResponseModel.class)
                .value(list -> {
                    assertFalse(list.isEmpty());
                    list.forEach(item -> assertTrue(item.getEmail() == null || item.getEmail().contains("@")));
                });
    }


    @Test
    void whenEmailIsEmpty_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Tom")
                .lastname("Brown")
                .email("")
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @Test
    void getAllLibrarians_shouldReturnJsonContentType() {
        webTestClient.get()
                .uri(BASE_URI)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void whenLibrarianNotFound_thenReturnErrorFormat() {
        webTestClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").value(msg -> assertTrue(((String) msg).contains(NOT_FOUND_ID)));
    }

    @Test
    void whenInvalidId_thenReturnErrorFormat() {
        webTestClient.get()
                .uri(BASE_URI + "/invalid-id")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("UNPROCESSABLE_ENTITY");
    }

    @Test
    void whenUnsupportedAccept_thenReturnNotAcceptable() {
        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void whenUnsupportedContentType_thenReturnUnsupportedMediaType() {
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue("<fake></fake>")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void whenDeletingWithoutId_thenReturnMethodNotAllowed() {
        webTestClient.delete()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void whenPhoneNumberIsNull_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Nora")
                .lastname("Jones")
                .email("nora.jones@example.com")
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenPositionIsNull_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Jake")
                .lastname("Miles")
                .email("jake.miles@example.com")
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void deleteThenGet_shouldReturnNotFound() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createThenRetrieveById_shouldReturnSameLibrarian() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Tony")
                .lastname("Stark")
                .email("tony.stark@library.com")
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .librarianPhoneNumber(new LibrarianPhoneNumber())
                .build();

        LibrarianResponseModel[] created = new LibrarianResponseModel[1];

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LibrarianResponseModel.class)
                .value(response -> created[0] = response);

        webTestClient.get()
                .uri(BASE_URI + "/" + created[0].getLibrarianId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(LibrarianResponseModel.class)
                .value(response -> {
                    assertEquals("Tony", response.getFirstname());
                    assertEquals("Stark", response.getLastname());
                });
    }

    @Test
    void whenLibrarianHasNullPhoneNumber_thenReturnUnprocessableEntity() {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Mark")
                .lastname("Twain")
                .email("mark.twain@example.com")
                .position(new Position(PositionEnum.LIBRARY_CLERK))
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void whenGetAllLibrarians_thenSizeMatchesRepository() {
        long expectedSize = repository.count();

        webTestClient.get()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LibrarianResponseModel.class)
                .value(list -> assertEquals(expectedSize, list.size()));
    }

    @Test
    void whenGettingLibrarianById_thenFieldsMatch() {
        webTestClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LibrarianResponseModel.class)
                .value(res -> {
                    assertNotNull(res.getEmail());
                    assertNotNull(res.getFirstname());
                    assertNotNull(res.getLastname());
                    assertNotNull(res.getPosition());
                });
    }

    @Test
    void whenInvalidUUIDOnGet_thenThrowInvalidInputException() {
        webTestClient.get()
                .uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").value(msg -> assertTrue(msg.toString().toLowerCase().contains("invalid")));
    }



}