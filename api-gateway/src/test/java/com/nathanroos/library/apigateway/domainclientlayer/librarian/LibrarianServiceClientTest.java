package com.nathanroos.library.apigateway.domainclientlayer.librarian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.HttpErrorInfo;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibrarianServiceClientTest {

    @InjectMocks
    private LibrarianServiceClient librarianServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private final String host = "localhost";
    private final String port = "8080";

    @BeforeEach
    void setUp() {
        librarianServiceClient = new LibrarianServiceClient(restTemplate, objectMapper, host, port);
    }

    @Test
    void testGetLibrarian_Success() {
        String id = "lib123";
        LibrarianResponseModel librarian = LibrarianResponseModel.builder().librarianId(id).build();
        when(restTemplate.getForObject(anyString(), eq(LibrarianResponseModel.class))).thenReturn(librarian);

        LibrarianResponseModel result = librarianServiceClient.getLibrarian(id);

        assertNotNull(result);
        assertEquals(id, result.getLibrarianId());
    }

    @Test
    void testGetLibrarian_NotFound() throws Exception {
        String id = "lib123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, "{\"message\":\"Not Found\"}".getBytes(), null);
        when(restTemplate.getForObject(anyString(), eq(LibrarianResponseModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/workers/" + id));

        assertThrows(NotFoundException.class, () -> librarianServiceClient.getLibrarian(id));
    }

    @Test
    void testGetLibrarian_ObjectMapperError() throws Exception {
        String id = "lib123";
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", null, "{}".getBytes(), null);

        // Mock RestTemplate throwing the HttpClientErrorException
        when(restTemplate.getForObject(anyString(), eq(LibrarianResponseModel.class)))
                .thenThrow(ex);

        // Use doAnswer or doThrow for checked exceptions on readValue()
        doAnswer(invocation -> {
            throw new IOException("Mapping failed");
        }).when(objectMapper).readValue(anyString(), eq(HttpErrorInfo.class));

        // Verify behavior
        assertThrows(NotFoundException.class, () -> librarianServiceClient.getLibrarian(id));
    }





    @Test
    void testGetWorkers_Success() {
        List<LibrarianResponseModel> workers = List.of(LibrarianResponseModel.builder().librarianId("lib123").build());
        ResponseEntity<List<LibrarianResponseModel>> response = new ResponseEntity<>(workers, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibrarianResponseModel> result = librarianServiceClient.getWorkers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetWorkers_Empty() {
        ResponseEntity<List<LibrarianResponseModel>> response = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibrarianResponseModel> result = librarianServiceClient.getWorkers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWorkers_HttpError() {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Error", null, "{}".getBytes(), null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.getWorkers());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }

    @Test
    void testCreateLibrarian_Success() {
        LibrarianRequestModel request = LibrarianRequestModel.builder().firstname("John").build();
        LibrarianResponseModel response = LibrarianResponseModel.builder().librarianId("lib123").build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibrarianResponseModel.class))).thenReturn(response);

        LibrarianResponseModel result = librarianServiceClient.createLibrarian(request);

        assertNotNull(result);
        assertEquals("lib123", result.getLibrarianId());
    }

    @Test
    void testCreateLibrarian_InvalidInput() throws Exception {
        LibrarianRequestModel request = LibrarianRequestModel.builder().firstname("John").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", null, "{}".getBytes(), null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibrarianResponseModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", "/workers"));

        assertThrows(InvalidInputException.class, () -> librarianServiceClient.createLibrarian(request));
    }

    @Test
    void testCreateLibrarian_ServerError() {
        LibrarianRequestModel request = LibrarianRequestModel.builder().firstname("John").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, "{}".getBytes(), null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibrarianResponseModel.class))).thenThrow(ex);

        assertThrows(HttpClientErrorException.class, () -> librarianServiceClient.createLibrarian(request));
    }

    @Test
    void testUpdateLibrarian_Success() {
        String id = "lib123";
        LibrarianRequestModel request = LibrarianRequestModel.builder().firstname("John").build();
        LibrarianResponseModel updated = LibrarianResponseModel.builder().librarianId(id).build();
        when(restTemplate.getForObject(anyString(), eq(LibrarianResponseModel.class))).thenReturn(updated);

        LibrarianResponseModel result = librarianServiceClient.updateLibrarian(id, request);

        assertNotNull(result);
        assertEquals(id, result.getLibrarianId());
    }

    @Test
    void testUpdateLibrarian_HttpError() throws Exception {
        String id = "lib123";
        LibrarianRequestModel request = LibrarianRequestModel.builder().firstname("John").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, "{}".getBytes(), null);
        doThrow(ex).when(restTemplate).put(anyString(), eq(request));
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/workers/" + id));

        assertThrows(NotFoundException.class, () -> librarianServiceClient.updateLibrarian(id, request));
    }

    @Test
    void testDeleteLibrarian_Success() {
        doNothing().when(restTemplate).delete(anyString());

        assertDoesNotThrow(() -> librarianServiceClient.deleteLibrarian("lib123"));
    }

    @Test
    void testDeleteLibrarian_NotFound() throws Exception {
        String id = "lib123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, "{}".getBytes(), null);
        doThrow(ex).when(restTemplate).delete(anyString());
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/workers/" + id));

        assertThrows(NotFoundException.class, () -> librarianServiceClient.deleteLibrarian(id));
    }




    @Test
    void testCreateLibrarian_ReturnsNull() {
        LibrarianRequestModel request = LibrarianRequestModel.builder().firstname("John").build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibrarianResponseModel.class)))
                .thenReturn(null);

        LibrarianResponseModel result = librarianServiceClient.createLibrarian(request);

        assertNull(result);  // Just assert it's null, no exception.
    }


    @Test
    void testUpdateLibrarian_NullResponse() {
        String librarianId = "lib123";
        LibrarianRequestModel request = LibrarianRequestModel.builder().firstname("John").build();

        when(restTemplate.getForObject(anyString(), eq(LibrarianResponseModel.class)))
                .thenReturn(null);

        LibrarianResponseModel result = librarianServiceClient.updateLibrarian(librarianId, request);

        assertNull(result);  // Confirm it's null but no exception.
    }


    @Test
    void testDeleteLibrarian_RuntimeException() {
        String librarianId = "lib123";
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, "Error".getBytes(), null);
        doThrow(ex).when(restTemplate).delete(anyString());

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> librarianServiceClient.deleteLibrarian(librarianId));
        assertTrue(thrown.getMessage().contains("500")); // Adjust based on actual error message
    }

    @Test
    void testGetWorkers_NullResponse() {
        ResponseEntity<List<LibrarianResponseModel>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<LibrarianResponseModel> result = librarianServiceClient.getWorkers();

        // Should return null or empty list depending on implementation
        assertTrue(result == null || result.isEmpty());
    }

    @Test
    void testGetLibrarian_ThrowsUnexpectedException() {
        String librarianId = "lib123";
        when(restTemplate.getForObject(anyString(), eq(LibrarianResponseModel.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(RuntimeException.class, () -> librarianServiceClient.getLibrarian(librarianId));
    }

    @Test
    void testCreateLibrarian_ThrowsUnexpectedException() {
        LibrarianRequestModel request = LibrarianRequestModel.builder().firstname("John").build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibrarianResponseModel.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(RuntimeException.class, () -> librarianServiceClient.createLibrarian(request));
    }

}
