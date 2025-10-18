package com.nathanroos.library.customersubdomain.PresentationLayer;

import com.nathanroos.library.customersubdomain.DataAccessLayer.LibraryAccountRepository;
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
@Sql({"/data-psql.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LibraryAccountControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LibraryAccountRepository repository;

    private final String BASE_URI = "/api/v1/accounts";
    private final String NOT_FOUND_ID = "84a8ec6e-2fdc-4c6d-940f-9b2274d44422";
    private final String INVALID_ID = "this-is-not-a-uuid";
    private final String VALID_ID = "84a8ec6e-2fdc-4c6d-940f-9b2274d44420";

    @Test
    void whenAccountExists_thenReturnAllVolunteer() {
        long sizeDB = this.repository.count();

        this.webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(LibraryAccountResponseModel.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertNotEquals(0, sizeDB);
                    assertEquals(sizeDB, list.size());
                    list.forEach((authorResponseModel) -> {
                        assertNotNull(authorResponseModel);
                        assertNotNull(authorResponseModel.getAccountId());
                        assertNotNull(authorResponseModel.getFirstname());
                        assertNotNull(authorResponseModel.getLastname());
                        assertNotNull(authorResponseModel.getEmail());
                        assertNotNull(authorResponseModel.getPhoneNumber());
                    });
                });
    }

    @Test
    void whenValidAccountId_thenReturnOk() {
        webTestClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(LibraryAccountResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getAccountId());
                    assertNotNull(response.getFirstname());
                    assertNotNull(response.getLastname());
                });
    }

    @Test
    void whenValidAccount_thenReturnCreated() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("123-456-7890")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(LibraryAccountResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getAccountId());
                    assertEquals(request.getFirstname(), response.getFirstname());
                    assertEquals(request.getLastname(), response.getLastname());
                });
    }

    @Test
    void whenValidAccountId_thenDeleteAccount() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown accountId: " + VALID_ID);
    }

    @Test
    void whenInvalidIdFormat_thenReturnUnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URI + "/invalid-id")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenAccountNotFound_thenReturn404() {
        webTestClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown accountId: " + NOT_FOUND_ID);
    }

    @Test
    void whenCustomerFirstNameIsInvalidOnPost_thenReturnUnprocessableEntity() {
        LibraryAccountRequestModel libraryAccountRequestModel = LibraryAccountRequestModel.builder()
                .firstname("")
                .lastname("Wallace")
                .build();

        this.webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(libraryAccountRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid firstName: " + libraryAccountRequestModel.getFirstname());
    }

    @Test
    void whenLastnameIsMissing_thenReturnUnprocessableEntity() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("Jane")
                .email("jane@example.com")
                .phoneNumber("123")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenInvalidEmail_thenReturnUnprocessableEntity() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("bademail")
                .phoneNumber("123")
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
                .jsonPath("$.message").isEqualTo("Invalid accountId: " + INVALID_ID);
    }

    @Test
    void whenNonexistentUUIDOnDelete_thenReturnNotFound() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown accountId: " + NOT_FOUND_ID);
    }

    @Test
    void whenEmailMissing_thenReturnUnprocessableEntity() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("Jane")
                .lastname("Doe")
                .phoneNumber("123-456-7890")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenExtraFieldProvided_thenIgnoreAndCreateAccount() {
        String payload = """
        {
            "firstname": "Anna",
            "lastname": "Smith",
            "email": "anna.smith@example.com",
            "phoneNumber": "123-456-7890",
            "extraField": "unexpected"
        }
        """;

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.firstname").isEqualTo("Anna");
    }

    @Test
    void whenAccountAlreadyDeleted_thenReturnNotFound() {
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
    void whenGetAfterDelete_thenReturnNotFound() {
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
    void whenPostWithLongNames_thenReturnCreated() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("A".repeat(50))
                .lastname("B".repeat(50))
                .email("longname@example.com")
                .phoneNumber("999-999-9999")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void whenEmptyPayload_thenReturnBadRequest() {
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }



    @Test
    void whenPostWithNullValues_thenReturnUnprocessableEntity() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname(null)
                .lastname(null)
                .email(null)
                .phoneNumber(null)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenPostWithWhitespaceNames_thenReturnUnprocessableEntity() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname(" ")
                .lastname(" ")
                .email("valid@example.com")
                .phoneNumber("222-222-2222")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenGetAllWithAcceptXml_thenReturnNotAcceptable() {
        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void whenDeleteWithGet_thenReturnMethodNotAllowed() {
        webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri(BASE_URI)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }


    @Test
    void whenPatchNotAllowed_thenReturn405() {
        webTestClient.patch()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void whenOptionsCalled_thenReturnOkOrHandled() {
        webTestClient.options()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void whenHeadCalled_thenReturnOkOrHeadersOnly() {
        webTestClient.head()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void whenValidUpdate_thenReturnUpdatedAccount() {
        LibraryAccountRequestModel updateRequest = LibraryAccountRequestModel.builder()
                .firstname("UpdatedFirstName")
                .lastname("UpdatedLastName")
                .email("updated.email@example.com")
                .phoneNumber("987-654-3210")
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LibraryAccountResponseModel.class)
                .value(response -> {
                    assertEquals("UpdatedFirstName", response.getFirstname());
                    assertEquals("UpdatedLastName", response.getLastname());
                    assertEquals("updated.email@example.com", response.getEmail());
                    assertEquals("987-654-3210", response.getPhoneNumber());
                });
    }

    @Test
    void whenUpdateWithInvalidIdFormat_thenReturnUnprocessableEntity() {
        LibraryAccountRequestModel updateRequest = LibraryAccountRequestModel.builder()
                .firstname("Test")
                .lastname("User")
                .email("test.user@example.com")
                .phoneNumber("123-456-7890")
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenUpdateNonExistentAccount_thenReturnNotFound() {
        LibraryAccountRequestModel updateRequest = LibraryAccountRequestModel.builder()
                .firstname("Test")
                .lastname("User")
                .email("test.user@example.com")
                .phoneNumber("123-456-7890")
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown accountId: " + NOT_FOUND_ID);
    }

    @Test
    void whenCreateAccountWithMissingEmail_thenReturnUnprocessableEntity() {
        LibraryAccountRequestModel request = LibraryAccountRequestModel.builder()
                .firstname("Jane")
                .lastname("Doe")
                .phoneNumber("123-456-7890")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenDeleteAccountTwice_thenSecondDeleteReturnsNotFound() {
        webTestClient.delete()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.delete()
                .uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unknown accountId: " + VALID_ID);
    }

    @Test
    void whenGetAllAccounts_thenContentTypeIsJson() {
        webTestClient.get()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void whenCreateAccountWithExtraFields_thenExtraFieldsAreIgnored() {
        String requestBody = """
        {
            "firstname": "Extra",
            "lastname": "Field",
            "email": "extra.field@example.com",
            "phoneNumber": "123-456-7890",
            "extraField": "shouldBeIgnored"
        }
        """;

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.extraField").doesNotExist();
    }

    @Test
    void whenUpdateAccountWithEmptyBody_thenReturnBadRequest() {
        webTestClient.put()
                .uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
