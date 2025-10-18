package com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.HttpErrorInfo;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LibrarianServiceClientTest {

    @InjectMocks
    private LibrarianServiceClient librarianServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Value("${app.libraryworker-service.host}")
    private String host = "localhost";

    @Value("${app.libraryworker-service.port}")
    private String port = "8080";

    @BeforeEach
    void setup() {
        librarianServiceClient = new LibrarianServiceClient(restTemplate, objectMapper, host, port);
    }

    @Test
    void testGetLibrarianById_Success() {
        String librarianId = "123";
        LibraryWorkerModel mockAdopter = LibraryWorkerModel.builder()
                .librarianId(librarianId)
                .firstname("John")
                .lastname("Doe")
                .build();
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class)))
                .thenReturn(mockAdopter);

        LibraryWorkerModel result = librarianServiceClient.getLibrarian(librarianId);

        assertNotNull(result);
        assertEquals(librarianId, result.getLibrarianId());
        assertEquals("John", result.getFirstname());
    }

    @Test
    void testGetAllAdopters_Success() {
        List<LibraryWorkerModel> adopters = List.of(LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe")
                .build());
        ResponseEntity<List<LibraryWorkerModel>> response = new ResponseEntity<>(adopters, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryWorkerModel> result = librarianServiceClient.getWorkers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testAddAdopter_Success() {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe")
                .build();

        LibraryWorkerModel response = LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe")
                .build();

        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryWorkerModel.class))).thenReturn(response);

        LibraryWorkerModel result = librarianServiceClient.createLibrarian(request);

        assertNotNull(result);
    }

    @Test
    void testUpdateAdopter_Success() {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe")
                .build();

        LibraryWorkerModel updated = LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe Updated")
                .build();

        String librarianId = "123";

        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class)))
                .thenReturn(updated);

        LibraryWorkerModel result = librarianServiceClient.updateLibrarian(librarianId, request);

        assertNotNull(result);
        assertEquals("John", result.getFirstname());  // Optional: add more assertions to check the data
    }


    @Test
    void testDeleteAdopter_Success() {
        doNothing().when(restTemplate).delete(anyString());

        assertDoesNotThrow(() -> librarianServiceClient.deleteLibrarian("123"));
    }

    @Test
    void testGetAdopterById_NotFound() throws Exception {
        String librarianId = "123";
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not found", "/some/path"));

        assertThrows(NotFoundException.class, () -> librarianServiceClient.getLibrarian(librarianId));
    }

    @Test
    void testAddAdopter_InvalidInput() throws Exception {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe")
                .build();
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryWorkerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/workers"));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.createLibrarian(request));
    }

    @Test
    void testDeleteAdopter_NotFound() throws Exception {
        String librarianId = "123";
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        doThrow(ex).when(restTemplate).delete(anyString());
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not found", "/workers/" + librarianId));

        assertThrows(NotFoundException.class, () -> librarianServiceClient.deleteLibrarian(librarianId));
    }

    @Test
    void testGetAllAdopters_HttpClientError() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(ex);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.getWorkers());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }

    @Test
    void testUpdateAdopter_NotFoundOnGet() throws Exception {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe")
                .build();

        String librarianId = "123";

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not found", "/workers/" + librarianId));

        assertThrows(NotFoundException.class, () -> librarianServiceClient.updateLibrarian(librarianId, request));
    }

    @Test
    void testGetLibrarianById_EmptyId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.getLibrarian(" "));
    }

    @Test
    void testUpdateLibrarian_EmptyId_ShouldThrowInvalidInput() {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe")
                .build();
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.updateLibrarian(" ", request));
    }

    @Test
    void testDeleteLibrarian_EmptyId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.deleteLibrarian(" "));
    }

    @Test
    void testCreateLibrarian_EmptyFields_ShouldStillSucceed() {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("")
                .firstname("")
                .lastname("")
                .build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryWorkerModel.class)))
                .thenReturn(request);

        LibraryWorkerModel result = librarianServiceClient.createLibrarian(request);
        assertNotNull(result);
        assertEquals("", result.getFirstname());
    }

    @Test
    void testGetAllLibrarians_NoContent_ShouldReturnEmptyList() {
        ResponseEntity<List<LibraryWorkerModel>> response = new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryWorkerModel> result = librarianServiceClient.getWorkers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testInvalidInputExceptionConstructors() {
        InvalidInputException ex1 = new InvalidInputException("Message");
        InvalidInputException ex2 = new InvalidInputException(new RuntimeException("Cause"));
        InvalidInputException ex3 = new InvalidInputException("Message", new RuntimeException("Cause"));
        assertNotNull(ex1);
        assertNotNull(ex2);
        assertNotNull(ex3);
    }

    @Test
    void testNotFoundExceptionConstructors() {
        NotFoundException ex1 = new NotFoundException("Message");
        NotFoundException ex2 = new NotFoundException(new RuntimeException("Cause"));
        NotFoundException ex3 = new NotFoundException("Message", new RuntimeException("Cause"));
        assertNotNull(ex1);
        assertNotNull(ex2);
        assertNotNull(ex3);
    }

    @Test
    void testGetLibrarianById_InvalidInput() throws Exception {
        String librarianId = "invalid!";
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/workers/" + librarianId));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.getLibrarian(librarianId));
    }

    @Test
    void testGetLibrarianById_OtherError() throws Exception {
        String librarianId = "error";
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class))).thenThrow(ex);

        assertThrows(RuntimeException.class, () -> librarianServiceClient.getLibrarian(librarianId));
    }



    @Test
    void testGetAllLibrarians_Success() {
        List<LibraryWorkerModel> expected = List.of(LibraryWorkerModel.builder()
                .librarianId("lib-123")
                .firstname("John")
                .lastname("Doe")
                .build());
        ResponseEntity<List<LibraryWorkerModel>> response = new ResponseEntity<>(expected, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryWorkerModel> result = librarianServiceClient.getWorkers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }


    @Test
    void testGetAllLibrarians_HttpClientError_InvalidInput() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/workers"));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.getWorkers());
    }

    @Test
    void testGetAllLibrarians_HttpClientError_NotFound() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not found", "/workers"));

        assertThrows(NotFoundException.class, () -> librarianServiceClient.getWorkers());
    }

    @Test
    void testGetAllLibrarians_HttpClientError_Other() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);

        assertThrows(RuntimeException.class, () -> librarianServiceClient.getWorkers());
    }

    // Negative test: getLibrarian(null)
    @Test
    void testGetLibrarianById_NullId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.getLibrarian(null));
    }

    // Negative test: getLibrarian with HttpClientErrorException NOT_FOUND
    @Test
    void testGetLibrarianById_NotFound() throws Exception {
        String librarianId = "nonexistent";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Librarian not found", "/workers/" + librarianId));

        assertThrows(NotFoundException.class, () -> librarianServiceClient.getLibrarian(librarianId));
    }

    // Negative test: getLibrarian with server error
    @Test
    void testGetLibrarianById_ServerError() {
        String librarianId = "error";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class))).thenThrow(ex);

        assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.getLibrarian(librarianId));
    }

    // Negative test: getWorkers() returns null response body
    @Test
    void testGetAllLibrarians_NullResponseBody() {
        ResponseEntity<List<LibraryWorkerModel>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryWorkerModel> result = librarianServiceClient.getWorkers();

        assertNotNull(result); // Should not be null, should return empty list
        assertTrue(result.isEmpty());
    }

    // Negative test: createLibrarian() returns HTTP error
    @Test
    void testCreateLibrarian_HttpError() throws Exception {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("dummy-id")
                .firstname("Dummy")
                .lastname("User")
                .build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryWorkerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/workers"));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.createLibrarian(request));
    }

    // Negative test: updateLibrarian() with null ID
    @Test
    void testUpdateLibrarian_NullId_ShouldThrowInvalidInput() {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("dummy-id")
                .firstname("Dummy")
                .lastname("User")
                .build();

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.updateLibrarian(null, request));
    }


    // Negative test: deleteLibrarian() with null ID
    @Test
    void testDeleteLibrarian_NullId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.deleteLibrarian(null));
    }

    @Test
    void testUpdateLibrarian_HttpError() throws Exception {
        String librarianId = "bad-id";
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("dummy-id")
                .firstname("Dummy")
                .lastname("User")
                .build();

        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.BAD_GATEWAY, "Bad Gateway", null, null, null);
        doThrow(ex).when(restTemplate).put(anyString(), eq(request));



        assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.updateLibrarian(librarianId, request));
    }

    // Additional tests to maximize coverage for LibraryWorker domain client layer

    // getLibrarian with empty ID
    @Test
    void testGetLibrarian_EmptyId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.getLibrarian(""));
    }

    // getWorkers returns empty list
    @Test
    void testGetWorkers_EmptyList() {
        ResponseEntity<List<LibraryWorkerModel>> response = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryWorkerModel> result = librarianServiceClient.getWorkers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // createLibrarian with null input
    @Test
    void testCreateLibrarian_NullInput_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.createLibrarian(null));
    }

    // createLibrarian happy path
    @Test
    void testCreateLibrarian_Success() {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("lib-123")
                .firstname("John")
                .lastname("Doe")
                .build();

        LibraryWorkerModel expectedResponse = LibraryWorkerModel.builder()
                .librarianId("lib-123")
                .firstname("John")
                .lastname("Doe")
                .build();

        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryWorkerModel.class)))
                .thenReturn(expectedResponse);

        LibraryWorkerModel result = librarianServiceClient.createLibrarian(request);

        assertNotNull(result);
        assertEquals("lib-123", result.getLibrarianId());
    }

    // updateLibrarian with null request
    @Test
    void testUpdateLibrarian_NullRequest_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.updateLibrarian("lib-123", null));
    }

    // updateLibrarian happy path
    @Test
    void testUpdateLibrarian_Success() {
        String librarianId = "lib-123";
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId(librarianId)
                .firstname("John")
                .lastname("Doe")
                .build();

        LibraryWorkerModel expectedResponse = LibraryWorkerModel.builder()
                .librarianId(librarianId)
                .firstname("John")
                .lastname("Doe")
                .build();

        // Correct: use doNothing() for void method
        doNothing().when(restTemplate).put(anyString(), eq(request));

        when(restTemplate.getForObject(contains(librarianId), eq(LibraryWorkerModel.class)))
                .thenReturn(expectedResponse);

        LibraryWorkerModel result = librarianServiceClient.updateLibrarian(librarianId, request);

        assertNotNull(result);
        assertEquals(librarianId, result.getLibrarianId());
    }


    // deleteLibrarian happy path
    @Test
    void testDeleteLibrarian_Success() {
        String librarianId = "lib-123";
        doNothing().when(restTemplate).delete(anyString());

        assertDoesNotThrow(() -> librarianServiceClient.deleteLibrarian(librarianId));
    }

    // Edge case: createLibrarian with incomplete fields (simulate server validation failure)
    @Test
    void testCreateLibrarian_IncompleteFields_ShouldThrowInvalidInput() throws Exception {
        LibraryWorkerModel incompleteRequest = LibraryWorkerModel.builder()
                .firstname("John")
                .build(); // missing last name and id

        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(incompleteRequest), eq(LibraryWorkerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input: missing fields", "/workers"));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.createLibrarian(incompleteRequest));
    }

    // Edge case: updateLibrarian with mismatched ID (simulate ID mismatch)
    @Test
    void testUpdateLibrarian_MismatchedId_ShouldThrowInvalidInput() throws Exception {
        String pathId = "lib-999";
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("lib-123") // mismatched with pathId
                .firstname("Jane")
                .lastname("Doe")
                .build();

        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "ID Mismatch", null, null, null);
        doThrow(ex).when(restTemplate).put(anyString(), eq(request));
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "ID mismatch error", "/workers/" + pathId));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.updateLibrarian(pathId, request));
    }


    // Edge case: getLibrarian with whitespace ID
    @Test
    void testGetLibrarian_WhitespaceId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.getLibrarian("   "));
    }

    @Test
    void testGetLibrarianById_BlankId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.getLibrarian("  "));
    }

    @Test
    void testUpdateLibrarian_BlankId_ShouldThrowInvalidInput() {
        LibraryWorkerModel dummy = LibraryWorkerModel.builder().librarianId("123").build();
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.updateLibrarian(" ", dummy));
    }

    @Test
    void testDeleteLibrarian_BlankId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.deleteLibrarian("   "));
    }

    @Test
    void testCreateLibrarian_InvalidData_ShouldThrowInvalidInput() throws Exception {
        LibraryWorkerModel invalidLibrarian = LibraryWorkerModel.builder().librarianId("").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(invalidLibrarian), eq(LibraryWorkerModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/workers"));
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.createLibrarian(invalidLibrarian));
    }

    @Test
    void testGetAllLibrarians_EmptyListResponse() {
        ResponseEntity<List<LibraryWorkerModel>> response = new ResponseEntity<>(List.of(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        List<LibraryWorkerModel> result = librarianServiceClient.getWorkers();
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list when no librarians found");
    }

    @Test
    void testGetLibrarianById_RestTemplateReturnsNull_ShouldThrowNotFound() {
        String librarianId = "nonexistent-id";
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class))).thenReturn(null);
        assertThrows(NotFoundException.class, () -> librarianServiceClient.getLibrarian(librarianId));
    }

    @Test
    void testUpdateLibrarian_ServerErrorWithoutJsonMessage() {
        String librarianId = "lib-500";
        LibraryWorkerModel request = LibraryWorkerModel.builder().librarianId(librarianId).build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        doThrow(ex).when(restTemplate).put(anyString(), eq(request));
        assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.updateLibrarian(librarianId, request));
    }

    @Test
    void testGetAllLibrarians_ServerErrorWithoutJsonMessage() {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);
        assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.getWorkers());
    }

    @Test
    void testCreateLibrarian_ServerErrorWithoutJsonMessage() {
        LibraryWorkerModel request = LibraryWorkerModel.builder().librarianId("lib-err").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryWorkerModel.class))).thenThrow(ex);
        assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.createLibrarian(request));
    }

    @Test
    void testDeleteLibrarian_ServerErrorWithoutJsonMessage() {
        String librarianId = "lib-err";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "Raw server error".getBytes(), null);
        doThrow(ex).when(restTemplate).delete(anyString());
        assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.deleteLibrarian(librarianId));
    }



    @Test
    void testGetLibrarian_NullResponse_ShouldThrowNotFoundException() {
        String librarianId = "missing-id";
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class)))
                .thenReturn(null);

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                librarianServiceClient.getLibrarian(librarianId)
        );
        assertEquals("Librarian not found for ID: " + librarianId, thrown.getMessage());
    }


    @Test
    void testDeleteLibrarian_WhitespaceId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.deleteLibrarian("   "));
    }


    @Test
    void testUpdateLibrarian_NullInput_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.updateLibrarian("123", null));
    }


    @Test
    void testUpdateLibrarian_WhitespaceId_ShouldThrowInvalidInput() {
        LibraryWorkerModel request = LibraryWorkerModel.builder()
                .librarianId("123")
                .firstname("John")
                .lastname("Doe")
                .build();
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.updateLibrarian("   ", request));
    }

    @Test
    void testHandleHttpClientException_NOT_FOUND() throws Exception {
        String librarianId = "nonexistent";

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", null, null, null);

        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class)))
                .thenThrow(ex);

        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "/test", "Not found"));

        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> librarianServiceClient.getLibrarian(librarianId));

        assertTrue(thrown.getMessage().contains("Not found"));
    }


    @Test
    void testHandleHttpClientException_UNPROCESSABLE_ENTITY() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", null, null, null);

        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/test", "Invalid input"));

        when(restTemplate.postForObject(anyString(), any(), eq(LibraryWorkerModel.class)))
                .thenThrow(ex);

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> librarianServiceClient.createLibrarian(
                        LibraryWorkerModel.builder().librarianId("dummy").firstname("First").lastname("Last").build()
                ));

        assertTrue(thrown.getMessage().contains("Invalid input"));
    }


    @Test
    void testHandleHttpClientException_OtherError() {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Error", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryWorkerModel.class))).thenThrow(ex);
        assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.getLibrarian("123"));
    }

    @Test
    void testGetWorkers_ResponseBodyNull() {
        ResponseEntity<List<LibraryWorkerModel>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        List<LibraryWorkerModel> result = librarianServiceClient.getWorkers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWorkers_NullResponse_ShouldReturnEmptyList() {
        ResponseEntity<List<LibraryWorkerModel>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<LibraryWorkerModel> result = librarianServiceClient.getWorkers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateLibrarian_InvalidInput_ShouldThrowInvalidInputException() throws Exception {
        LibraryWorkerModel request = LibraryWorkerModel.builder().librarianId("id").firstname("fn").lastname("ln").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", null, null, null);
        doThrow(ex).when(restTemplate).put(anyString(), eq(request));
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/workers", "Invalid input"));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.updateLibrarian("id", request));
    }

    @Test
    void testDeleteLibrarian_InvalidInput_ShouldThrowInvalidInputException() throws Exception {
        String librarianId = "bad-id";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", null, null, null);
        doThrow(ex).when(restTemplate).delete(anyString());
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/workers", "Invalid input"));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.deleteLibrarian(librarianId));
    }

    @Test
    void testCreateLibrarian_NullInput_ShouldThrowInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> librarianServiceClient.createLibrarian(null));
    }

    @Test
    void testUpdateLibrarian_PutSuccess_ButGetReturnsNull_ShouldThrowNotFound() {
        String librarianId = "lib-123";
        LibraryWorkerModel request = LibraryWorkerModel.builder().librarianId(librarianId).build();
        doNothing().when(restTemplate).put(anyString(), eq(request));
        when(restTemplate.getForObject(contains(librarianId), eq(LibraryWorkerModel.class)))
                .thenReturn(null); // Simulate getLibrarian returns null

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> librarianServiceClient.updateLibrarian(librarianId, request));
        assertEquals("Librarian not found for ID: " + librarianId, thrown.getMessage());
    }

    @Test
    void testPOJOs() {
        WorkerAddress address = new WorkerAddress("123", "Main St", "City", ProvinceEnum.ONTARIO, "A1A1A1");
        assertEquals("123", address.getStreetNumber());
        assertEquals(ProvinceEnum.ONTARIO, address.getProvince());

        Position position = new Position();
        position.positionTitle = PositionEnum.LIBRARY_CLERK;
        assertEquals(PositionEnum.LIBRARY_CLERK, position.positionTitle);
    }










}
